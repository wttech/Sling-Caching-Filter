## Introduction

Cache bundle is a OSGi bundle that provides caching mechanisms for pages, components or parts of code. Cache bundle uses cache filter.

## Installation

Add following dependency to your project:

    <dependency>
        <groupId>com.cognifide.cq</groupId>
        <artifactId>sling-caching-filter</artifactId>
        <version>0.8.2-SNAPSHOT</version>
    </dependency>

or download the sources and use the `maven-sling-plugin`:

    mvn clean package sling:install

## Cache filter

### General description

Cache filter is able to cache all pages/renderers/components. It is configured to filter all sling requests on the component level. The only limit is that the whole component is being cached.

It is strongly discouraged to enable cache filter on the author instance since this produces issues with CQ js code.

### Configuration

Cache filter is configured in two places: in the OSGi console and inside the application which components are being cached.

#### Cached application internal configuration

The application internal configuration is placed inside components/renderers `.content.xml` files. Filter configuration is stored inside a cache XML element placed inside the `jcr:root` root element. The cache element has the following XML attributes:

| attribute name                   | attribute type | required | description | default value |
| -------------------------------  | -------------- | -------- | ----------- | ------------- |
| jcr:primaryType                  | String         | yes      | jcr add on  | nt:unstructured |
| cog:cacheEnabled                 | boolean        | yes      | enables/disables caching of given component | false |
| cog:validityTime                 | integer        | no       | specifies cache entry validity time (in seconds) | duration property read from the OSGi console |
| cog:cacheLevel                   | String         | no       | specifies the level of component caching | -1 |
| cog:invalidateOnSelf             | boolean        | no       | when set to true cached instance will be refreshed if it has been changed | true |
| cog:invalidateOnReferencedFields | String[]       | no       | List of component fields that store links to content/configuration/etc. pages. Links from those fields are loaded and each content change inside nodes pointed to by those links will invalidate cache of the current component | empty list |
| cog:invalidateOnPaths            | String[]       | no       | List of paths (regular expressions). If a path of any changed JCR node mathes any path from the list then the cache of the current component is invalidated | empty list |

Allowed values for the `cog:cacheLevel`:

* -1 - Each instance is cached separately (resource path is used to create cache key).
* 0 - There is only one instance of the component on the whole site. To determine which instance is cached, the first-renderer rule applies (the first rednered component is cached and used on other pages)
* any positive value - Component is cached per path. The value of cache level determines how many parts of the request URI (separated by the "/" character) will be used to generate cache key. For example, when this value is set to 3 and the path is /content/acme/en_gb/home.html, then only "/content/acme/en_gb" will be used to generate the key meaning that component will be cached per language.

An example configuration of Showcase's header component (`.content.xml`) could look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root xmlns:cog="http://www.cognifide.com/cog/1.0" xmlns:cq="http://www.day.com/jcr/cq/1.0" xmlns:jcr="http://www.jcp.org/jcr/1.0"
  cq:isContainer="{Boolean}false"
  jcr:primaryType="cq:Component"
  jcr:title="Header"
  componentGroup="showcase">
  <cache
    cog:cacheEnabled="{Boolean}true"
    cog:cacheLevel="-1"
    cog:invalidateOnPaths="[/content/showcase(/.*)?]"
    cog:invalidateOnSelf="{Boolean}false"
    jcr:primaryType="nt:unstructured" />
</jcr:root>
```

Please notice the obligatory cog namespace definition in the second line.

#### OSGi console

OSGi console allows to modify the following properties:

* Enabled - enables/disables cache filter
* Resource types - List of cached components types. Each entry on the list has the following format: `component_type[:duration[:level]]` where
* component_type - component type with full path. when present it acts as if the component had it's property enabled set to true inside it's .conent.xml file. Adding this line overrides the the enabled property set (if present) in the .conent.xml files.
* duration - overrides the validTime property set inside the .content.xml file
* level - overrides the cacheLevel property set inside the .content.xml file
* Capacity - cache capacity
* Memory - store cache in memory/on disk
* Algorithm - cache entry removal algorithm
* Duration - Maximum default time (in seconds) after which cache entry must be refreshed
* Language segment - index of the path element that stores the language code

For other properties see the [OSCache docs](http://svn.apache.org/repos/asf/db/ojb/trunk/src/config/oscache.properties).

## TODOs

Things/ideas left out for the future:
* implement (if ever required) different caching strategies for different selectors
* add configuration of paths that are (or are not) filtered by the cache filter
