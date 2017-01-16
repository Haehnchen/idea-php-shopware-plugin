package de.espend.idea.shopware.navigation;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ConstantFunction;
import com.intellij.util.indexing.FileBasedIndexImpl;
import com.jetbrains.php.PhpIcons;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.smarty.SmartyFile;
import com.jetbrains.smarty.SmartyFileType;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.index.SmartyExtendsStubIndex;
import de.espend.idea.shopware.index.SmartyIncludeStubIndex;
import de.espend.idea.shopware.util.ShopwareUtil;
import de.espend.idea.shopware.util.SmartyPattern;
import de.espend.idea.shopware.util.TemplateUtil;
import fr.adrienbrault.idea.symfony2plugin.util.PhpElementsUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class SmartyTemplateLineMarkerProvider implements LineMarkerProvider {

    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> psiElements, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {

        if(psiElements.size() == 0 || !ShopwareProjectComponent.isValidForProject(psiElements.get(0))) {
            return;
        }

        Set<VirtualFile> extendsPathFiles = null;

        for(PsiElement psiElement: psiElements) {

            if(psiElement instanceof SmartyFile) {
                attachFileContextMaker((SmartyFile) psiElement, lineMarkerInfos);
            }

            if(SmartyPattern.getBlockPattern().accepts(psiElement)) {
                attachTemplateBlocks(psiElement, lineMarkerInfos);
            }

            if(SmartyPattern.getBlockPattern().accepts(psiElement)) {

                // cache template extends path
                if(extendsPathFiles == null) {
                    extendsPathFiles = new HashSet<>();
                    getImplementedBlocks(psiElement.getProject(), psiElement.getContainingFile().getVirtualFile(), extendsPathFiles, 10);
                }

                attachImplementsBlocks(psiElement, lineMarkerInfos, extendsPathFiles);
            }

        }

    }

    private void attachFileContextMaker(SmartyFile smartyFile, @NotNull Collection<LineMarkerInfo> lineMarkerInfos) {
        List<GotoRelatedItem> gotoRelatedItems = new ArrayList<>();

        attachController(smartyFile, gotoRelatedItems);
        attachInclude(smartyFile, gotoRelatedItems);
        attachExtends(smartyFile, gotoRelatedItems);

        if(gotoRelatedItems.size() == 0) {
            return;
        }

        // only one item dont need popover
        if(gotoRelatedItems.size() == 1) {
            lineMarkerInfos.add(RelatedPopupGotoLineMarker.getSingleLineMarker(smartyFile, lineMarkerInfos, gotoRelatedItems.get(0)));
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

        return new LineMarkerInfo<>(lineMarkerTarget, lineMarkerTarget.getTextOffset(), ShopwarePluginIcons.SHOPWARE_LINEMARKER, 6, new ConstantFunction<>(title), new fr.adrienbrault.idea.symfony2plugin.dic.RelatedPopupGotoLineMarker.NavigationHandler(gotoRelatedItems));
    }

    public void attachImplementsBlocks(PsiElement psiElement, Collection<LineMarkerInfo> lineMarkerInfos, Set<VirtualFile> virtualFiles) {

        if(virtualFiles.size() == 0) {
            return;
        }

        VirtualFile virtualFile = psiElement.getContainingFile().getVirtualFile();
        if(virtualFiles.contains(virtualFile)) {
            virtualFiles.remove(virtualFile);
        }

        final String blockName = psiElement.getText();
        if(StringUtils.isBlank(blockName)) {
            return;
        }

        final Collection<PsiElement> targets = new ArrayList<>();

        for(VirtualFile virtualTemplate: virtualFiles) {
            PsiFile psiFile = PsiManager.getInstance(psiElement.getProject()).findFile(virtualTemplate);
            if(psiFile != null) {
                psiFile.acceptChildren(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {

                        if(SmartyPattern.getBlockPattern().accepts(element)) {
                            String text = element.getText();
                            if(blockName.equalsIgnoreCase(text)) {
                                targets.add(element);
                            }

                        }

                        super.visitElement(element);
                    }
                });
            }
        }

        if(targets.size() == 0) {
            return;
        }

        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTED).
            setTargets(targets).
            setTooltipText("Navigate to block");

        lineMarkerInfos.add(builder.createLineMarkerInfo(psiElement));


    }

    private void getImplementedBlocks(final Project project, VirtualFile virtualFile, final Set<VirtualFile> templatePathFiles, int depth) {

        if(templatePathFiles.contains(virtualFile) || depth-- <= 0) {
            return;
        }

        final String templateName = TemplateUtil.getTemplateName(project, virtualFile);
        if(templateName == null) {
            return;
        }

        final int finalDepth = depth;
        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyExtendsStubIndex.KEY, new HashSet<>(Arrays.asList(templateName)), virtualFile1 -> {

            templatePathFiles.add(virtualFile1);
            getImplementedBlocks(project, virtualFile1, templatePathFiles, finalDepth);

            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(project), SmartyFileType.INSTANCE));
    }

    public void attachTemplateBlocks(PsiElement psiElement, Collection<LineMarkerInfo> lineMarkerInfos) {

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

        String relativeFilename = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(relativeFilename == null) {
            return;
        }

        Pattern pattern = Pattern.compile(".*[/]*(frontend|backend|core)/(\\w+)/(\\w+)\\.tpl");
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

        Method method = phpClass.findMethodByName(action + "Action");
        if(method != null) {
            gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(method, "Navigate to action").withIcon(PhpIcons.METHOD, PhpIcons.METHOD));
            return;
        }

        // fallback to class
        gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(phpClass, "Navigate to class").withIcon(PhpIcons.CLASS, PhpIcons.CLASS));

    }

    public void attachInclude(final SmartyFile smartyFile, final List<GotoRelatedItem> gotoRelatedItems) {

        final String templateName = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(templateName == null) {
            return;
        }

        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyIncludeStubIndex.KEY, new HashSet<>(Arrays.asList(templateName)), virtualFile -> {

            PsiFile psiFile = PsiManager.getInstance(smartyFile.getProject()).findFile(virtualFile);
            if(psiFile != null) {

                for(PsiElement psiElement: getIncludePsiElement(psiFile, templateName)) {
                    gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiElement, "Navigate to include").withIcon(PhpIcons.IMPLEMENTED, PhpIcons.IMPLEMENTED));
                }

            }

            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(smartyFile.getProject()), SmartyFileType.INSTANCE));

    }

    public void attachExtends(final SmartyFile smartyFile, final List<GotoRelatedItem> gotoRelatedItems) {

        final String templateName = TemplateUtil.getTemplateName(smartyFile.getProject(), smartyFile.getVirtualFile());
        if(templateName == null) {
            return;
        }

        FileBasedIndexImpl.getInstance().getFilesWithKey(SmartyExtendsStubIndex.KEY, new HashSet<>(Arrays.asList(templateName)), virtualFile -> {

            PsiFile psiFile = PsiManager.getInstance(smartyFile.getProject()).findFile(virtualFile);
            if(psiFile != null) {
                gotoRelatedItems.add(new RelatedPopupGotoLineMarker.PopupGotoRelatedItem(psiFile, TemplateUtil.getTemplateName(psiFile.getProject(), psiFile.getVirtualFile())).withIcon(PhpIcons.IMPLEMENTED, PhpIcons.IMPLEMENTED));
            }

            return true;
        }, GlobalSearchScope.getScopeRestrictedByFileTypes(GlobalSearchScope.allScope(smartyFile.getProject()), SmartyFileType.INSTANCE));

    }

    private static List<PsiElement> getIncludePsiElement(PsiFile psiFile, final String templateName) {
        final List<PsiElement> psiElements = new ArrayList<>();

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
