package de.espend.idea.shopware.util;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.parser.PhpElementTypes;
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression;
import com.jetbrains.php.lang.psi.elements.Field;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import fr.adrienbrault.idea.symfony2plugin.Symfony2InterfacesUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ThemeUtil {

    public static void visitThemeAssetsFile(@NotNull PhpClass phpClass, final @NotNull ThemeAssetVisitor themeAssetVisitor) {

        PsiDirectory parent = phpClass.getContainingFile().getParent();
        if(parent == null) {
            return;
        }

        final VirtualFile publicFolder = VfsUtil.findRelativeFile(parent.getVirtualFile(), "frontend", "_public");
        if(publicFolder == null) {
            return;
        }

        // collect on project template dir
        VfsUtil.visitChildrenRecursively(publicFolder, new VirtualFileVisitor() {
            @Override
            public boolean visitFile(@NotNull VirtualFile virtualFile) {

                if(!"js".equals(virtualFile.getExtension())) {
                    return true;
                }

                String relative = VfsUtil.getRelativePath(virtualFile, publicFolder, '/');
                if(relative == null) {
                    return true;
                }

                return themeAssetVisitor.visit(virtualFile, relative);
            }
        });


    }

    public static interface ThemeAssetVisitor {
        public boolean visit(@NotNull VirtualFile virtualFile, @NotNull String path);
    }

    public static void collectThemeJsFieldReferences(@NotNull StringLiteralExpression element, @NotNull ThemeAssetVisitor visitor) {

        PsiElement arrayValue = element.getParent();
        if(arrayValue.getNode().getElementType() != PhpElementTypes.ARRAY_VALUE) {
            return;
        }

        PsiElement arrayCreation = arrayValue.getParent();
        if(!(arrayCreation instanceof ArrayCreationExpression)) {
            return;
        }

        PsiElement classField = arrayCreation.getParent();
        if(!(classField instanceof Field)) {
            return;
        }

        if(!"javascript".equals(((Field) classField).getName())) {
            return;
        }


        PhpClass phpClass = PsiTreeUtil.getParentOfType(classField, PhpClass.class);
        if(phpClass == null || !new Symfony2InterfacesUtil().isInstanceOf(phpClass, "\\Shopware\\Components\\Theme")) {
            return;
        }

        visitThemeAssetsFile(phpClass, visitor);
    }

    @NotNull
    public static PsiElementPattern.Capture<PsiElement> getJavascriptClassFieldPattern() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                PlatformPatterns.psiElement(PhpElementTypes.ARRAY_VALUE).withParent(
                    PlatformPatterns.psiElement(ArrayCreationExpression.class).withParent(
                        PlatformPatterns.psiElement(Field.class).withName("javascript")
                    )
                )
            )
        );

    }

    public static PsiElementPattern.Capture<PsiElement> getThemeExtendsPattern() {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression.class).withParent(
                PlatformPatterns.psiElement(Field.class).withName("extend")
            )
        );
    }

}
