package de.espend.idea.shopware.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import de.espend.idea.shopware.completion.ShopwarePhpCompletion;
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ConfigUtil {

    public static void visitNamespace(@NotNull Project project, @NotNull Consumer<Pair<String, PhpClass>> pairConsumer) {
        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Plugin")) {
            pairConsumer.accept(Pair.create(phpClass.getName(), phpClass));
        }
    }

    public static void visitNamespaceConfigurations(@NotNull Project project, @NotNull String namespace, @NotNull Consumer<Pair<String, XmlTag>> pairConsumer) {
        for(PhpClass phpClass: PhpIndex.getInstance(project).getAllSubclasses("\\Shopware\\Components\\Plugin")) {
            if(!namespace.equalsIgnoreCase(phpClass.getName())) {
                continue;
            }

            PsiDirectory parent = phpClass.getContainingFile().getParent();
            if(parent == null) {
                continue;
            }

            VirtualFile resources = VfsUtil.findRelativeFile(parent.getVirtualFile(), "Resources", "config.xml");
            if(resources == null) {
                continue;
            }

            PsiFile file = PsiManager.getInstance(project).findFile(resources);
            if(!(file instanceof XmlFile)) {
                continue;
            }

            XmlTag rootTag = ((XmlFile) file).getRootTag();
            if(rootTag == null) {
                continue;
            }

            XmlTag elements = rootTag.findFirstSubTag("elements");
            if(elements == null) {
                continue;
            }

            for (XmlTag element : elements.findSubTags("element")) {
                XmlTag xmlTag = element.findFirstSubTag("name");
                if(xmlTag != null) {
                    String text = xmlTag.getValue().getText();
                    if(StringUtils.isNotBlank(text)) {
                        pairConsumer.accept(Pair.create(text, xmlTag));
                    }
                }
            }
        }
    }

    /**
     * 
     */
    @Nullable
    public static String getNamespaceFromConfigValueParameter(@NotNull StringLiteralExpression parent) {
        MethodMatcher.MethodMatchParameter match = MethodMatcher.getMatchedSignatureWithDepth(parent, ShopwarePhpCompletion.CONFIG_NAMESPACE, 1);
        if(match == null) {
            return null;
        }

        PsiElement parameterList = parent.getParent();
        if(!(parameterList instanceof ParameterList)) {
            return null;
        }

        PsiElement[] funcParameters = ((ParameterList) parameterList).getParameters();
        if(funcParameters.length == 0 || !(funcParameters[0] instanceof StringLiteralExpression)) {
            return null;
        }

        String namespace = ((StringLiteralExpression) funcParameters[0]).getContents();
        if(StringUtils.isBlank(namespace)) {
            return null;
        }

        return namespace;
    }
}
