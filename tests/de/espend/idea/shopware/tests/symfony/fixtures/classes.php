<?php

namespace Enlight\Event {
    interface SubscriberInterface
    {
        public static function getSubscribedEvents();
    };
}

namespace Foo {

    use Enlight\Event\SubscriberInterface;

    class MySubscriber implements SubscriberInterface
    {
        public static function getSubscribedEvents()
        {
            return [
                'Enlight_Bootstrap_InitResource_foobar.my.subscriber' => 'foobar',
            ];
        }

        public function foobar() {
            return new MySubscriber();
        }
    }
}

