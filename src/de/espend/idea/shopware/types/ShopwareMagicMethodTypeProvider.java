package de.espend.idea.shopware.types;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.jetbrains.php.lang.psi.elements.impl.FunctionReferenceImpl;
import com.jetbrains.php.lang.psi.resolve.types.PhpTypeProvider2;
import de.espend.idea.shopware.ShopwareProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.Settings;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShopwareMagicMethodTypeProvider implements PhpTypeProvider2 {

    final static char TRIM_KEY = '\u0180';

    @Override
    public char getKey() {
        return '\u0169';
    }

    @Nullable
    @Override
    public String getType(PsiElement e) {

        if (DumbService.getInstance(e.getProject()).isDumb() || !Settings.getInstance(e.getProject()).pluginEnabled) {
            return null;
        }

        // container calls are only on "get" methods
        if(!(e instanceof FunctionReferenceImpl)) {
            return null;
        }

        if(!"Shopware".equals(((FunctionReferenceImpl) e).getName())) {
            return null;
        }

        return ((FunctionReferenceImpl) e).getSignature();
    }

    @Override
    public Collection<? extends PhpNamedElement> getBySignature(String expression, Project project) {


        List<PhpNamedElement> elements = new ArrayList<PhpNamedElement>();

        PhpIndex phpIndex = PhpIndex.getInstance(project);
        Collection<? extends PhpNamedElement> phpNamedElementCollections = phpIndex.getBySignature(expression, null, 0);
        if(phpNamedElementCollections.size() == 0) {
            return null;
        }

        elements.addAll(phpNamedElementCollections);

        File file = new File(ShopwareProjectComponent.getMagicFilePathname(project));
        if(!file.exists()) {
            return elements;
        }

        VirtualFile fileByIoFile = VfsUtil.findFileByIoFile(file, true);
        if(fileByIoFile == null) {
            return elements;
        }

        PsiFile psiFile = PsiManager.getInstance(project).findFile(fileByIoFile);
        PhpClass phpClass = PsiTreeUtil.findChildOfType(psiFile, PhpClass.class);
        if(phpClass != null) {
            elements.add(phpClass);
        }

        return elements;
    }

}
