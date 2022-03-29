/*
jQWidgets v4.2.1 (2016-Aug)
Copyright (c) 2011-2016 jQWidgets.
License: http://jqwidgets.com/license/
*/

(function ($) {
    'use strict';
    if (!$.jqx.elements) {
        $.jqx.elements = new Array();
    }

    // jqxCalendar
    $.jqx.elements.push(
    {
        name: "jqxCalendar",
        tagName: "jqx-calendar",
        template: "<div></div>",
        attributeSync: true,
        properties:
        {
            disabled:
            {
                attributeSync: false
            },
            width:
            {
                type: "length"
            },
            height:
            {
                type: "length"
            },
            min: { type: "date" },
            max: { type: "date" },
            value: { type: "date" }
        }
    });
    // jqxComplexInput
    $.jqx.elements.push(
    {
        name: "jqxComplexInput",
        tagName: "jqx-complexinput",
        template: "<div><input/><div></div></div>",
        attributeSync: true,
        properties:
        {
            width:
            {
                type: "length"
            },
            height:
            {
                type: "length"
            }
        }
    });

    if (document.registerElement) {
        if (!Object.is) {
            Object.is = function (x, y) {
                // SameValue algorithm
                if (x === y) { // Steps 1-5, 7-10
                    // Steps 6.b-6.e: +0 != -0
                    return x !== 0 || 1 / x === 1 / y;
                } else {
                    // Step 6.a: NaN == NaN
                    return x !== x && y !== y;
                }
            };
        }
        $(document).ready(function () {
            $(document).on("WebComponentsReady", function () {
                
            });

            $.each($.jqx.elements, function () {
                var name = this.name;
                var metaInfo = this;
                var proto = Object.create(HTMLElement.prototype);
                proto.name = name;
                proto.instances = new Array();
                var propertyToAttributeConfig = {};
                var attributeTable = (function () {
                    var attrs = {},
                        addAttributeConfig = function (tagName, attributeName, attributeConfig) {
                            if (attrs[tagName] === undefined) {
                                attrs[tagName] = {};
                            }

                            attrs[tagName][attributeName] = attributeConfig;
                        },

                        getAttributeConfig = function (tagName, attributeName) {
                            if (attrs[tagName] === undefined || attrs[tagName][attributeName] === undefined) {
                                return undefined;
                            } else {
                                return attrs[tagName][attributeName];
                            }
                        },

                        getAttributeList = function (tagName) {
                            return attrs[tagName];
                        };

                    return {
                        addAttributeConfig: addAttributeConfig,
                        getAttributeConfig: getAttributeConfig,
                        getAttributeList: getAttributeList
                    };
                }());

                var properties = $.jqx["_" + name].prototype.defineInstance();

                if (name == "jqxDockingLayout") {
                    properties = $.extend(properties, $.jqx["_jqxLayout"].prototype.defineInstance());
                }
                if (name == "jqxToggleButton" || name == "jqxRepeatButton") {
                    properties = $.extend(properties, $.jqx["_jqxButton"].prototype.defineInstance());
                }
                if (name == "jqxTreeGrid") {
                    properties = $.extend(properties, $.jqx["_jqxDataTable"].prototype.defineInstance());
                }

                $.each(properties, function (propertyName, defaultValue) {
                    var metaPropertyInfo = metaInfo.properties[propertyName];
                    var attributeName = propertyName.split(/(?=[A-Z])/).join('-').toLowerCase();
                    var defaultValueType = typeof defaultValue;
                    var attributeSync = (metaPropertyInfo && metaPropertyInfo.attributeSync) || metaInfo.attributeSync;
                    var privatePropertyName = "_" + propertyName;

                    if (metaPropertyInfo && metaPropertyInfo.type) {
                        defaultValueType = metaPropertyInfo.type;
                    }

                    var attributeConfig = {
                        defaultValue: defaultValue,
                        type: defaultValueType,
                        propertyName: propertyName,
                        attributeSync: attributeSync
                    };

                    attributeTable.addAttributeConfig(metaInfo.tagName, attributeName, Object.freeze(attributeConfig));
                    propertyToAttributeConfig[propertyName] = attributeName;

                    var updatePropertyAction = function (value) {
                        this[privatePropertyName] = value;
                        var instance = this.getInstance();
                        instance[propertyName] = value;
                    }

                    Object.defineProperty(proto, propertyName, {
                        configurable: false,
                        enumerable: true,
                        get: function () {
                            return this[privatePropertyName];
                        },
                        set: function (value) {
                            updatePropertyAction.call(this, value);
                        }
                    });
                });

                proto.propertyUpdated = function (propertyName, oldValue, newValue) {
                }

                proto.getAttributeTyped = function (attributeName, propertyInfo) {
                    return this.attributeStringToTypedValue(attributeName, propertyInfo, this.getAttribute(attributeName));
                };
                proto.setAttributeTyped = function (attributeName, propertyInfo, value) {
                    var str, currVal;

                    // Get the attribute in case user is using this standalone (so corresponding property may not exist)
                    currVal = this.getAttributeTyped(attributeName, propertyInfo);

                    str = this.typedValueToAttributeString(value);

                    if (str === undefined) {
                        this.removeAttribute(attributeName);
                    } else {
                        this.setAttribute(attributeName, str);
                    }
                }
                proto.typedValueToAttributeString = function (value) {
                    var type = typeof value;

                    if (type === 'boolean') {
                        if (value) {
                            return '';
                        } else {
                            return undefined;
                        }
                    } else if (type === 'number') {
                        if (Object.is(value, -0)) {
                            return '-0';
                        } else {
                            return value.toString();
                        }
                    } else if (type === 'string' || type === 'length') {
                        return value;
                    } else if (type === 'object') {
                        return JSON.stringify(value, function (k, v) {
                            if (typeof v === 'number') {
                                if (isFinite(v) === false) {
                                    return v.toString();
                                } else if (Object.is(v, -0)) {
                                    return '-0';
                                }
                            }

                            return v;
                        });
                    }
                }
                proto.attributeStringToTypedValue = function (attributeName, propertyInfo, str) {
                    if (propertyInfo.type === 'boolean') {
                        if (str === '' || str === attributeName || str === 'true') {
                            return true;
                        } else {
                            return false;
                        }
                    } else if (propertyInfo.type === 'number') {
                        if (str === null || str === undefined) {
                            return undefined;
                        } else {
                            return parseFloat(str);
                        }
                    } else if (propertyInfo.type === 'string') {
                        if (str === null || str === undefined) {
                            return undefined;
                        } else {
                            return str;
                        }
                    }
                    else if (propertyInfo.type === "length") {
                        if (str === null) return null;
                        if (str !== null && str.indexOf("px") >= 0) {
                            return parseFloat(str);
                        } if (str !== null && str.indexOf("%") >= 0) {
                            return str;
                        }
                        if (!isNaN(parseFloat(str))) {
                            return parseFloat(str);
                        }
                        return str;
                    }
                    return undefined;
                }

                proto.createInstance = function (settings) {
                    this.createWidget(settings);
                }

                proto.createdCallback = function () {
                    var that = this;
                    var width = null;
                    var height = null;
                    var container, helper, initInstance, attributes;
                    var events = [];
                    var canUpdateContainerSize = true;
                    var attrList = attributeTable.getAttributeList(metaInfo.tagName);
                    var settings = {};
                    var defaultSettings = {};
                    var template = metaInfo.template;
                    for (var currAttrName in attrList) {
                        if (attrList.hasOwnProperty(currAttrName) && that.hasAttribute(currAttrName)) {
                            var currAttrConfig = attrList[currAttrName];
                            var currAttrValue = that.getAttributeTyped(currAttrName, currAttrConfig);
                            var currPropValue;

                            if (currAttrValue === undefined) {
                                currPropValue = currAttrConfig.defaultValue;
                            } else {
                                currPropValue = currAttrValue;
                            }

                            settings[currAttrConfig.propertyName] = currPropValue;
                        }
                    }
                    attributes = that.attributes;
                    for (var currAttrName in attributes) {
                        var attr = attributes[currAttrName];
                        if (attr && attr.name && attr.name.indexOf("on-") >= 0) {
                            var attrValue = attr.value;
                            var value = "";
                            if (attrValue.indexOf("(") >= 0) {
                                value = attrValue.substring(0, attrValue.indexOf("("));
                            }
                            events.push({name: attr.name.substring(3), handler: value});
                        }
                    }
                    var nodeElement = function (template) {
                        var fragment = document.createDocumentFragment();
                        var div = document.createElement("div");
                        fragment.appendChild(div);
                        var rxhtmlTag = /<(?!area|br|col|embed|hr|img|input|link|meta|param)(([\w:]+)[^>]*)\/>/gi;
                        var rtagName = /<([\w:]+)/;
                        template = template.replace(rxhtmlTag, "<$1></$2>");
                        var tag = (rtagName.exec(template) || ["", ""])[1].toLowerCase();
                        var wrap = [0, "", ""];
                        var depth = wrap[0];
                        div.innerHTML = wrap[1] + template + wrap[2];
                        while (depth--) {
                            div = div.lastChild;
                        }

                        template = div.childNodes;
                        div.parentNode.removeChild(div);
                        nodeElement = template[0];
                        return nodeElement;
                    }(template);

                    container = nodeElement;
                 
                    var createInstance = function(defaultSettings)
                    {
                        if (!that.id) {
                            that.id = $.jqx.utilities.createId();
                        }
                        container.id = $.jqx.utilities.createId();
                        that.appendChild(container);
                        $.extend(settings, defaultSettings);
                        if (template.indexOf("button") >= 0 || template.indexOf("input") == 1 || template.indexOf("textarea") >= 0) {
                            that.style.display = "inline";
                        }
                        else {
                            that.style.display = "block";
                        }

                        var updateContainerSize = function (propertyName, value) {
                            if (!canUpdateContainerSize) {
                                return;
                            }

                            if (typeof value === "string" && value.indexOf("%") >= 0) {
                                that.style[propertyName] = value;
                            }
                            else if (typeof value === "string" && value.indexOf("px") >= 0) {
                                that.style[propertyName] = value;
                            }
                            else if (value) {
                                that.style[propertyName] = value + "px";
                            }
                            else if (that.style[propertyName]) {
                                that.style[propertyName] = null;
                            }
                        }

                        updateContainerSize("width", settings.width);
                        updateContainerSize("height", settings.height);

                        helper = new jqxHelper(that);
                        helper.data(that, "jqxWidget", { element: that });

                        width = helper.width();
                        height = helper.height();
                        settings.width = width;
                        settings.height = height;

                        var widget = window[name](container, settings);
                        for (var obj in widget) {
                            if ($.type(widget[obj]) == "function") {
                                if (obj.indexOf("_") >= 0) {
                                    continue;
                                }
                                if (obj === "getInstance")
                                    continue;

                                that[obj] = $.proxy(widget[obj], widget);
                            }
                        }
                        for(var i= 0; i < events.length; i++)
                        {
                            var eventObj = events[i];
                            widget.on(eventObj.name, function (event) {
                                if (window[eventObj.handler] && event.args) {
                                    window[eventObj.handler].apply(that, [event]);
                                }
                            });
                        // widget.on(
                        }

                        widget.widgetInstance.propertyUpdated = function (propertyName, oldValue, newValue) {
                            var attrName = propertyToAttributeConfig[propertyName];
                            var attrConfig = attributeTable.getAttributeConfig(metaInfo.tagName, attrName);
                            if (attrConfig.attributeSync) {
                                that.setAttributeTyped(attrName, attrConfig, newValue);
                            }
                            if (propertyName === "width" || propertyName === "height") {
                                updateContainerSize(propertyName, newValue);
                            }

                            that.propertyUpdated(propertyName, oldValue, newValue);
                        }

                        var updateWidgetSize = function () {
                            canUpdateContainerSize = false;
                            width = helper.width();
                            height = helper.height();
                            widget.width = width;
                            widget.height = height;
                            canUpdateContainerSize = true;
                        }
                        $.jqx.utilities.resize(helper, function () {
                            updateWidgetSize();
                        });
                        helper.sizeChanged(function () {
                            updateWidgetSize();
                        });
                    

                        proto.instances[that.id] = widget;
                        // invoked when the instance is constructed.
                        if (that.hasAttribute("created-instance")) {
                            var readyName = that.getAttribute("created-instance");
                            var readyFuncName = readyName.substring(0, readyName.length - 2);
                            window[readyFuncName].apply(that);
                        }
                    }
                    that.createWidget = createInstance;

                    initInstance = !that.hasAttribute("auto-create") || that.attributeStringToTypedValue("auto-create", { type: "boolean" }, that.getAttribute("auto-create"));
                
                    // invoked when the element will be constructed. Allows you create the widget on demand.
                    if (that.hasAttribute("create-instance") && !initInstance) {
                        var readyName = that.getAttribute("create-instance");
                        var readyFuncName = readyName.substring(0, readyName.length-2);
                        window[readyFuncName].apply(that, [function (initSettings) {
                            that.createInstance(initSettings);
                        }]);
                    }

                    // used for getting a javascript object for the initialization process.
                    if (that.hasAttribute("settings")) {
                        var settingsName = that.getAttribute("settings");
                        defaultSettings = window[settingsName];
                    }

                    if (!initInstance) {
                        return;
                    }

                    createInstance(defaultSettings);       
                }

                proto.getInstance = function () {
                    var element = proto.instances[this.id];
                    return element;
                }

                proto.attributeChangedCallback = function (attrName, oldVal, newVal) {
                    var attrConfig = attributeTable.getAttributeConfig(metaInfo.tagName, attrName);
                }

                var element = document.registerElement(metaInfo.tagName,
                {
                    prototype: proto
                });
                return element;
            });
        });
    }
})(jqxBaseFramework);
