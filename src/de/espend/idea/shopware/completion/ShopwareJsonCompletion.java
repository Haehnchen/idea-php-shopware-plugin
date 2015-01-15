package de.espend.idea.shopware.completion;

import com.google.gson.Gson;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.impl.JSObjectLiteralExpressionImpl;
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import de.espend.idea.shopware.ShopwarePluginIcons;
import de.espend.idea.shopware.ShopwareProjectComponent;
import de.espend.idea.shopware.completion.dict.SwPluginJson;
import de.espend.idea.shopware.completion.dict.SwPluginProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ShopwareJsonCompletion extends CompletionContributor{

    public static final String[] LOCALES = new String[]{"de", "en", "fr", "it", "es"};
    private static SwPluginJson PLUGIN_JSON;

    public ShopwareJsonCompletion() {

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(JSPropertyImpl.class).withParent(
                PlatformPatterns.psiElement(JSObjectLiteralExpressionImpl.class).withParent(
                    PlatformPatterns.psiFile()
                )
            )
        ).inFile(PlatformPatterns.psiFile().withName("plugin.json")),
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    String jsonContent;
                    try {
                        jsonContent = StreamUtil.readText(ShopwareJsonCompletion.class.getResourceAsStream("/resources/plugin-info-schema.json"), "utf-8");
                    } catch (IOException e) {
                        return;
                    }

                    for(Map.Entry<String, SwPluginProperty> entry: getPluginJsonTemplate(jsonContent).getProperties().entrySet()) {
                        result.addElement(LookupElementBuilder.create(entry.getKey())
                            .withIcon(ShopwarePluginIcons.SHOPWARE)
                            .withTypeText(entry.getValue().getDescription(), true)
                            .withTailText("(" + entry.getValue().getType() + ")", true)
                        );
                    }

                }
            }
        );

        extend(
            CompletionType.BASIC, PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(JSPropertyImpl.class).withParent(
                PlatformPatterns.psiElement(JSObjectLiteralExpressionImpl.class).withParent(
                    JSPropertyImpl.class
                )
            )
        ).inFile(PlatformPatterns.psiFile().withName("plugin.json"))
            ,
            new CompletionProvider<CompletionParameters>() {
                @Override
                protected void addCompletions(final @NotNull CompletionParameters parameters, ProcessingContext context, final @NotNull CompletionResultSet result) {

                    PsiElement originalPosition = parameters.getOriginalPosition();
                    if(originalPosition == null || !ShopwareProjectComponent.isValidForProject(originalPosition)) {
                        return;
                    }

                    PsiElement jsProperty = originalPosition.getParent();

                    Map<String, String[]> map = new HashMap<String, String[]>();
                    map.put("compatibility", new String[] {"minimumVersion", "maximumVersion", "blacklist"});
                    map.put("label", LOCALES);
                    map.put("changelogs", LOCALES);

                    if(jsProperty instanceof JSPropertyImpl) {
                        PsiElement jsObjectLiteral = jsProperty.getParent();
                        if(jsObjectLiteral instanceof JSObjectLiteralExpressionImpl) {
                            PsiElement jsObjectLiteralParent = jsObjectLiteral.getParent();
                            if(jsObjectLiteralParent instanceof JSPropertyImpl) {
                                String var = ((JSPropertyImpl) jsObjectLiteralParent).getName();
                                if(var != null && map.containsKey(var)) {
                                    for(String propertyName: map.get(var)) {
                                        result.addElement(LookupElementBuilder.create(propertyName)
                                            .withIcon(ShopwarePluginIcons.SHOPWARE)
                                        );
                                    }
                                }
                            }
                        }
                    }

                }
            }
        );

    }

    private SwPluginJson getPluginJsonTemplate(String jsonContent) {

        if(PLUGIN_JSON == null) {
            PLUGIN_JSON = new Gson().fromJson(jsonContent, SwPluginJson.class);
        }

        return PLUGIN_JSON;
    }

}
