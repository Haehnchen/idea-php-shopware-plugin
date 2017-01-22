
//{namespace name="backend/foobar/namespace"}

Ext.define('Foobar', {
    border: {
        html: '{s name="start_accept"}{/s}',
        html1: '{s name="start_accept" namespace="backend/foobar"}{/s}',
        html2: '<div class="image"><img src="'+feature3+'" /></div>' +
        '<h4 class="feature-title">{s name="filter_feature"}{/s}</h4>',
        html2: '<div class="image"><img src="'+feature3+'" /></div>' +
        '<h4 class="feature-title">{s name="filter_feature" namespace = "foobar"}{/s}</h4>'
    }
});