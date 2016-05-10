<?php

class MySubscriber
{

    /**
     * {@inheritdoc}
     */
    public static function getSubscribedEvents()
    {
        return [
            'Enlight_Bootstrap_InitResource_foobar' => 'foobar',
        ];
    }

    public function foobar()
    {
    }
}
