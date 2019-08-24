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

        Fo()->Config()->get('foo_incomplete');

        $foo = $this->config;

        $foo->get('variableConfigFo');
        $config->get('variableConfig');
        $cfg->get('variableConfigCfg');
    }
}