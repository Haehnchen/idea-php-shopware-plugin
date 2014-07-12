package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.index.SmartyBlockStubIndex;
import de.espend.idea.shopware.index.SmartyIncludeStubIndex;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmartyTemplateLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {

        for(PsiElement psiElement: psiElements) {

            if(psiElement instanceof SmartyFile) {
                attachController((SmartyFile) psiElement, lineMarkerInfos);
            }

            if(psiElement instanceof SmartyFile) {
                attachInclude((SmartyFile) psiElement, lineMarkerInfos);
            }

            if(SmartyPattern.getBlockPattern().accepts(psiElement)) {
                attachTemplateBlocks(psiElement, lineMarkerInfos);
            }

        }
    }

    public void attachTemplateBlocks(PsiElement psiElement, Collection<LineMarkerInfo> lineMarkerInfos) {

        if(!ShopwareProjectComponent.isValidForProject(psiElement)) {
            return;
        }

        SmartyBlockGoToHandler goToHandler = new SmartyBlockGoToHandler();
        PsiElement[] gotoDeclarationTargets = goToHandler.getGotoDeclarationTargets(psiElement, 0, null);

        if(gotoDeclarationTargets == null || gotoDeclarationTargets.length == 0) {
            return;
        }

        List<PsiElement> psiElements = Arrays.asList(gotoDeclarationTargets);
        if(psiElements.size() == 0) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.OVERRIDES).
            setTargets(psiElements).
            setTooltipText("Navigate to block");

        lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));

    }

    public void attachController(SmartyFile smartyFile, Collection<LineMarkerInfo> lineMarkerInfos) {

        if(!ShopwareProjectComponent.isValidForProject(smartyFile)) {
            return;
        }

        String relativeFilename = VfsUtil.getRelativePath(smartyFile.getVirtualFile(), smartyFile.getProject().getBaseDir(), '/');
        if(relativeFilename == null) {
            return;
        }

        Pattern pattern = Pattern.compile(".*/(frontend|backend|core)/(\\w+)/(\\w+)\\.tpl");
        Matcher matcher = pattern.matcher(relativeFilename);

        if(!matcher.find()) {
            return;
        }

        // Shopware_Controllers_Frontend_Account
        String moduleName = ShopwareUtil.toCamelCase(matcher.group(1), false);
        String controller = ShopwareUtil.toCamelCase(matcher.group(2), false);
        String action = ShopwareUtil.toCamelCase(matcher.group(3), true);

        // build class name
        String className = String.format("\\Shopware_Controllers_%s_%s", moduleName, controller);
        PhpClass phpClass = PhpElementsUtil.getClassInterface(smartyFile.getProject(), className);
        if(phpClass == null) {
            return;
        }

        Method method = PhpElementsUtil.getClassMethod(phpClass, action + "Action");
        if(method != null) {
            NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.METHOD).
                setTargets(method).
                setTooltipText("Navigate to controller");

            lineMarkerInfos.add(builder.createLineMarkerInfo(smartyFile));

            return;
        }

        // fallback to class
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.CLASS).
            setTargets(phpClass).
            setTooltipText("Navigate to class");

        lineMarkerInfos.add(builder.createLineMarkerInfo(smartyFile));

    }

    public void attachInclude(final SmartyFile smartyFile, Collection<LineMarkerInfo> lineMarkerInfos) {

        final String templateName = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(templateName == null) {
            return;
        }

        final List<PsiElement> targets = new ArrayList<PsiElement>();

        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyIncludeStubIndex.KEY, new HashSet<String>(Arrays.asList(templateName)), new Processor<VirtualFile>() {
            @Override
            public boolean process(VirtualFile virtualFile) {

                PsiFile psiFile = PsiManager.getInstance(smartyFile.getProject()).findFile(virtualFile);
                if(psiFile != null) {
                    targets.addAll(getIncludePsiElement(psiFile, templateName));
                }

                return true;
            }
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(smartyFile.getProject()), SmartyFileType.INSTANCE));

        if(targets.size() == 0) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTED).
            setTargets(targets).
            setTooltipText("Navigate to include");

        lineMarkerInfos.add(builder.createLineMarkerInfo(smartyFile));

    }

    private static List<PsiElement> getIncludePsiElement(PsiFile psiFile, final String templateName) {
        final List<PsiElement> psiElements = new ArrayList<PsiElement>();

        psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
            @Override
            public void visitElement(PsiElement element) {

                if(SmartyPattern.getFileIncludePattern().accepts(element)) {
                    String text = element.getText();
                    if(templateName.equalsIgnoreCase(text)) {
                        psiElements.add(element);
                    }

                }

                super.visitElement(element);
            }
        });

        return psiElements;
    }

}
