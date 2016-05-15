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
            'Enlight_Bootstrap_InitResource_foobar_array' => ['foobar_array', -111],
            
            'Enlight_Bootstrap_AfterInitResource_foobar_after_init' => 'foobar_after_init',
            'Enlight_Bootstrap_AfterRegisterResource_foobar_register_resource' => 'foobar_register_resource',
        ];
    }

    public function foobar()
    {
    }

    public function foobar_array()
    {
    }
}
