#!/usr/bin/env sh

READMEMD="$(cd "$(dirname "$0")"; pwd)/README.md"

cat <<EOF > "$READMEMD"
# Classic Icon Set

This is a modernized version of the original icon set from openHAB 1.x.
The set is provided with the distribution in both the PNG and SVG file format.

EOF

for icon in $(ls icons/*.png | sort -V); do
  name=$(basename "$icon")
  echo "Adding icon '$name'"
  if [ "$name" = "none.png" ] || [ "$name" = "none.svg" ]; then continue; fi
  allIcons="$allIcons$name,"
done

allIcons=${allIcons:: -1}

cat <<EOF >> "$READMEMD"
{% assign allIconsStr = "$allIcons" %}
{% assign allIcons = allIconsStr | split: ',' %}

{% for icon in allIcons %}
  {% assign iconLower = icon | downcase | split: "." %}
  {% assign iconWithoutExt = iconLower[0] %}
  {% assign allIconsWithoutExtensionStr = allIconsWithoutExtensionStr | append: iconWithoutExt | append: ',' %}
{% endfor %}
{% assign allIconsWithoutExtension = allIconsWithoutExtensionStr | split: ',' %}


## Places

{% for category in site.data.categories_places %}
    {% assign categoryNamesStr = categoryNamesStr | append: category.name | downcase | append: ',' %}
{% endfor %}
{% assign placesCategoryNames = categoryNamesStr | split: ',' %}

<div id="iconset-preview-locations" class="icons">
{% for category in placesCategoryNames %}
  {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
  {% if allIconsWithoutExtension contains category %}
    {% assign iconSrc = "icons/" | append: category | append: ".png" %}
  {% endif %}
  <figure>
    <img src="{{iconSrc}}" alt="{{category}}" title="{{category}}">
    <figcaption>{{category}}</figcaption>
  </figure>
{% endfor %}
</div>

## Things

{% assign categoryNamesStr = "" %}
{% for category in site.data.categories_thing %}
    {% assign categoryNamesStr = categoryNamesStr | append: category.name | downcase | append: ',' %}
{% endfor %}
{% assign thingCategoryNames = categoryNamesStr | split: ',' %}

<div id="iconset-preview-things" class="icons">
{% for category in thingCategoryNames %}
  {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
  {% if allIconsWithoutExtension contains category %}
    {% assign iconSrc = "icons/" | append: category | append: ".png" %}
  {% endif %}

  {% assign altText = "" %}
  {% for i in allIcons %}
    {% assign prefix = category | append: "-" %}    
    {% if i contains prefix %}
      {% assign iWithoutExt = i | split: "." %}
      {% assign altText = altText | append: iWithoutExt[0] | append: " " %}
    {% endif %}
  {% endfor %}
  <figure>
    <img src="{{iconSrc}}" alt="{{altText}}" title="{{altText}}">
    <figcaption>{{category}}</figcaption>
  </figure>
{% endfor %}
</div>

## Channels

{% for category in site.data.categories %}
    {% assign typesStr = typesStr | append: category.type | append: ',' %}
{% endfor %}
{% assign types = typesStr | split: ',' | uniq %}

{% for type in types %}

#### {{type}}

  {% assign channelCategoryNamesStr = "" %}
  {% for category in site.data.categories %}
    {% if category.type == type %}
      {% assign channelCategoryNamesStr = channelCategoryNamesStr | append: category.name | downcase | append: ',' %}
    {% endif %}
  {% endfor %}
  {% assign channelCategoryNames = channelCategoryNamesStr | split: ',' %}
  {% assign channelCategories = channelCategories | concat: channelCategoryNames %}

  <div id="iconset-preview-channels" class="icons">
  {% for channelCategory in channelCategoryNames %}
    {% assign iconSrc = base | append: "/img/icon_no_category.png" %}
    {% if allIconsWithoutExtension contains channelCategory %}
      {% assign iconSrc = "icons/" | append: channelCategory | append: ".png" %}
    {% endif %}

    {% assign altText = "" %}
    {% for i in allIcons %}
      {% assign prefix = channelCategory | append: "-" %}    
      {% if i contains prefix %}
        {% assign iWithoutExt = i | split: "." %}
        {% assign altText = altText | append: iWithoutExt[0] | append: " " %}
      {% endif %}
    {% endfor %}

    <figure>
      <img src="{{iconSrc}}" alt="{{altText}}" title="{{altText}}">
      <figcaption>{{channelCategory}}</figcaption>
    </figure>
  {% endfor %}
  </div>

{% endfor %}

## Other Icons

{% assign allCategories = thingCategoryNames | concat: channelCategories | concat: placesCategoryNames | sort | uniq %}

<div id="iconset-preview-other" class="icons">
{% for icon in allIcons %}
  {% assign categoryLower = icon | downcase | split: "." %}
  {% assign plainCategory = categoryLower[0] %}

  {% assign otherIcon = true %}
  {% for catWithIcon in allCategories %}
    {% if catWithIcon.size <= plainCategory.size %}
      {% assign plainCategoryStart = plainCategory | truncate: catWithIcon.size, "" %}
      {% if plainCategoryStart == catWithIcon %}
        {% assign otherIcon = false %}
        {% break %}
      {% endif %}
    {% endif %}
  {% endfor %}

  {% if otherIcon == false %}
    {% continue %}
  {% endif %}

  {% unless icon contains "-" %}

    {% assign altText = "" %}
    {% for i in allIcons %}
      {% assign prefix = plainCategory | append: "-" %}    
      {% if i contains prefix %}
        {% assign iWithoutExt = i | split: "." %}
        {% assign altText = altText | append: iWithoutExt[0] | append: " " %}
      {% endif %}
    {% endfor %}
  
    <figure>
      <img src="icons/{{icon}}" alt="{{altText}}" title="{{altText}}">
      <figcaption>{{plainCategory}}</figcaption>
    </figure>
  {% endunless %}
{% endfor %}
</div>

EOF

echo "Finished."
