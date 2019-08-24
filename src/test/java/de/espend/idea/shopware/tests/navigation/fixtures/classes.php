<?php

namespace Enlight\Event
{
    interface SubscriberInterface {}
}

namespace
{
    class Enlight_Controller_Action{}

    class Shopware_Controllers_Widgets_Listing extends Enlight_Controller_Action
    {
        public function topSellerAction()
        {
        }
    }

    class Shopware_Controllers_Frontend_FrontendListing extends Enlight_Controller_Action
    {
        public function topSellerFrontendAction()
        {
        }
    }
}