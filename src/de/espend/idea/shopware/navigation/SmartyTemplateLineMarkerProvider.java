package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
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
                attachFileContextMaker((SmartyFile) psiElement, lineMarkerInfos);
            }

            if(SmartyPattern.getBlockPattern().accepts(psiElement)) {
                attachTemplateBlocks(psiElement, lineMarkerInfos);
            }

        }
    }

    private void attachFileContextMaker(SmartyFile smartyFile, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
        List<GotoRelatedItem> gotoRelatedItems = new ArrayList<GotoRelatedItem>();

        attachController(smartyFile, gotoRelatedItems);
        attachInclude(smartyFile, gotoRelatedItems);

        if(gotoRelatedItems.size() == 0) {
            return;
        }


        lineMarkerInfos.add(getRelatedPopover("Related Files", "", smartyFile, gotoRelatedItems));

    }

    private LineMarkerInfo getRelatedPopover(String singleItemTitle, String singleItemTooltipPrefix, PsiElement lineMarkerTarget, List<GotoRelatedItem> gotoRelatedItems) {

        // single item has no popup
        String title = singleItemTitle;
        if(gotoRelatedItems.size() == 1) {
            String customName = gotoRelatedItems.get(0).getCustomName();
            if(customName != null) {
                title = String.format(singleItemTooltipPrefix, customName);
            }
        }

        return new LineMarkerInfo<PsiElement>(lineMarkerTarget, lineMarkerTarget.getTextOffset(), ShopwarePluginIcons.SHOPWARE_LINEMARKER, 6, new ConstantFunction<PsiElement, String>(title), new fr.adrienbrault.idea.symfony2plugin.dic.RelatedPopupGotoLineMarker.NavigationHandler(gotoRelatedItems));
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

    public void attachController(SmartyFile smartyFile, final List<GotoRelatedItem> gotoRelatedItems) {

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
            gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(method, null).withIcon(PhpIcons.METHOD, PhpIcons.METHOD));
            return;
        }

        // fallback to class
        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(phpClass, null).withIcon(PhpIcons.CLASS, PhpIcons.CLASS));

    }

    public void attachInclude(final SmartyFile smartyFile, final List<GotoRelatedItem> gotoRelatedItems) {

        final String templateName = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(templateName == null) {
            return;
        }

        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyIncludeStubIndex.KEY, new HashSet<String>(Arrays.asList(templateName)), new Processor<VirtualFile>() {
            @Override
            public boolean process(VirtualFile virtualFile) {

                PsiFile psiFile = PsiManager.getInstance(smartyFile.getProject()).findFile(virtualFile);
                if(psiFile != null) {

                    for(PsiElement psiElement: getIncludePsiElement(psiFile, templateName)) {
                        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiElement, null).withIcon(PhpIcons.IMPLEMENTED, PhpIcons.IMPLEMENTED));
                    }

                }

                return true;
            }
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(smartyFile.getProject()), SmartyFileType.INSTANCE));

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
