<?php

class Shopware {
    /**
     * @return \Shopware_Components_Config
     */
    public function Config()
    {

    }
}

function Shopware(){
    return new Shopware();
}

class sAdmin {
    /** @var Shopware_Components_Config */
    private $config;

    public function foo()
    {
        $this->config->get('sBASEFILE');
        Shopware()->Config()->get('globalConfig');

        $config = $this->config;

        $config->get('variableConfig');
    }
}