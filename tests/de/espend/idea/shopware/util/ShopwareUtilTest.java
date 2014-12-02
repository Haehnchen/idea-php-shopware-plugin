package de.espend.idea.shopware.util;

import org.junit.Assert;
import org.junit.Test;

public class ShopwareUtilTest extends Assert {

    @Test
    public void testGetCamelizeHook() {
        assertEquals("adminRegenerateSessionIdAfter", ShopwareUtil.getCamelizeHook("sAdmin::regenerateSessionId::after"));
        assertEquals("enlightControllerActionPostDispatchSecureFrontendDetail", ShopwareUtil.getCamelizeHook("Enlight_Controller_Action_PostDispatchSecure_Frontend_Detail"));
        assertEquals("shopwareModelsAttributeOrderPostRemove", ShopwareUtil.getCamelizeHook("Shopware\\Models\\Attribute\\Order::postRemove"));
        assertEquals("adminRegenerateSessionIdAfter", ShopwareUtil.getCamelizeHook("sAdmin::regenerateSessionId::after"));
        assertEquals("shopwareControllersBackendArticlePreparePricesAssociatedDataReplace", ShopwareUtil.getCamelizeHook("Shopware_Controllers_Backend_Article::preparePricesAssociatedData::replace"));
    }

    @Test
    public void testGetHookCompletionNameCleanup() {

        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("sAdmin::regenerateSessionId::after")).contains("onAdminRegenerateSessionIdAfter"));

        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("Enlight_Controller_Action_PostDispatchSecure_Frontend_Detail")).contains("onEnlightControllerActionPostDispatchSecureFrontendDetail"));
        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("Enlight_Controller_Action_PostDispatchSecure_Frontend_Detail")).contains("onActionPostDispatchSecureFrontendDetail"));
        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("Enlight_Controller_Action_PostDispatchSecure_Frontend_Detail")).contains("onControllerActionPostDispatchSecureFrontendDetail"));

        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("Shopware_Controllers_Backend_Article::preparePricesAssociatedData::replace")).contains("onControllersBackendArticlePreparePricesAssociatedDataReplace"));
        assertTrue(ShopwareUtil.getHookCompletionNameCleanup(ShopwareUtil.getCamelizeHook("Shopware_Controllers_Backend_Article::preparePricesAssociatedData::replace")).contains("onBackendArticlePreparePricesAssociatedDataReplace"));

    }

}
