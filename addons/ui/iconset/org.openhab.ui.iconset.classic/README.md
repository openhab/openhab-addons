# Classic Icon Set

This is a modernized version of the original icon set from openHAB 1.x.
The set is provided with the distribution in both the PNG and SVG file format.

{% assign allIconsStr = "alarm.png,attic.png,baby_1.png,baby_2.png,baby_3.png,baby_4.png,baby_5.png,baby_6.png,bath.png,battery.png,batterylevel.png,batterylevel-0.png,batterylevel-10.png,batterylevel-20.png,batterylevel-30.png,batterylevel-40.png,batterylevel-50.png,batterylevel-60.png,batterylevel-70.png,batterylevel-80.png,batterylevel-90.png,batterylevel-100.png,battery-0.png,battery-10.png,battery-20.png,battery-30.png,battery-40.png,battery-50.png,battery-60.png,battery-70.png,battery-80.png,battery-90.png,battery-100.png,bedroom.png,bedroom_blue.png,bedroom_orange.png,bedroom_red.png,blinds.png,blinds-0.png,blinds-10.png,blinds-20.png,blinds-30.png,blinds-40.png,blinds-50.png,blinds-60.png,blinds-70.png,blinds-80.png,blinds-90.png,blinds-100.png,bluetooth.png,boy_1.png,boy_2.png,boy_3.png,boy_4.png,boy_5.png,boy_6.png,calendar.png,camera.png,carbondioxide.png,cellar.png,chart.png,cinema.png,cinemascreen.png,cinemascreen-0.png,cinemascreen-10.png,cinemascreen-20.png,cinemascreen-30.png,cinemascreen-40.png,cinemascreen-50.png,cinemascreen-60.png,cinemascreen-70.png,cinemascreen-80.png,cinemascreen-90.png,cinemascreen-100.png,cistern.png,cistern-0.png,cistern-10.png,cistern-20.png,cistern-30.png,cistern-40.png,cistern-50.png,cistern-60.png,cistern-70.png,cistern-80.png,cistern-90.png,cistern-100.png,climate.png,climate-on.png,colorlight.png,colorpicker.png,colorwheel.png,contact.png,contact-ajar.png,contact-closed.png,contact-open.png,corridor.png,door.png,door-closed.png,door-open.png,dryer.png,dryer-0.png,dryer-1.png,dryer-2.png,dryer-3.png,dryer-4.png,dryer-5.png,energy.png,error.png,fan.png,fan_box.png,fan_ceiling.png,faucet.png,fire.png,fire-off.png,fire-on.png,firstfloor.png,flow.png,flowpipe.png,frontdoor.png,frontdoor-closed.png,frontdoor-open.png,garage.png,garagedoor.png,garagedoor-0.png,garagedoor-10.png,garagedoor-20.png,garagedoor-30.png,garagedoor-40.png,garagedoor-50.png,garagedoor-60.png,garagedoor-70.png,garagedoor-80.png,garagedoor-90.png,garagedoor-100.png,garagedoor-ajar.png,garagedoor-closed.png,garagedoor-open.png,garage_detached.png,garage_detached_selected.png,garden.png,gas.png,girl_1.png,girl_2.png,girl_3.png,girl_4.png,girl_5.png,girl_6.png,greenhouse.png,groundfloor.png,group.png,heating.png,heating-0.png,heating-20.png,heating-40.png,heating-60.png,heating-80.png,heating-100.png,heating-off.png,heating-on.png,house.png,humidity.png,humidity-0.png,humidity-10.png,humidity-20.png,humidity-30.png,humidity-40.png,humidity-50.png,humidity-60.png,humidity-70.png,humidity-80.png,humidity-90.png,humidity-100.png,incline.png,keyring.png,kitchen.png,lawnmower.png,light.png,lightbulb.png,light-0.png,light-10.png,light-20.png,light-30.png,light-40.png,light-50.png,light-60.png,light-70.png,light-80.png,light-90.png,light-100.png,light-off.png,light-on.png,line.png,line-decline.png,line-incline.png,line-stagnation.png,lock.png,lock-closed.png,lock-open.png,lowbattery.png,lowbattery-off.png,lowbattery-on.png,man_1.png,man_2.png,man_3.png,man_4.png,man_5.png,man_6.png,mediacontrol.png,microphone.png,moon.png,motion.png,movecontrol.png,network.png,network-off.png,network-on.png,niveau.png,office.png,oil.png,outdoorlight.png,pantry.png,parents-off.png,parents_1_1.png,parents_1_2.png,parents_1_3.png,parents_1_4.png,parents_1_5.png,parents_1_6.png,parents_2_1.png,parents_2_2.png,parents_2_3.png,parents_2_4.png,parents_2_5.png,parents_2_6.png,parents_3_1.png,parents_3_2.png,parents_3_3.png,parents_3_4.png,parents_3_5.png,parents_3_6.png,parents_4_1.png,parents_4_2.png,parents_4_3.png,parents_4_4.png,parents_4_5.png,parents_4_6.png,parents_5_1.png,parents_5_2.png,parents_5_3.png,parents_5_4.png,parents_5_5.png,parents_5_6.png,parents_6_1.png,parents_6_2.png,parents_6_3.png,parents_6_4.png,parents_6_5.png,parents_6_6.png,party.png,pie.png,piggybank.png,player.png,poweroutlet.png,poweroutlet-off.png,poweroutlet-on.png,poweroutlet_au.png,poweroutlet_eu.png,poweroutlet_uk.png,poweroutlet_us.png,presence.png,presence-off.png,pressure.png,price.png,projector.png,pump.png,qualityofservice.png,qualityofservice-0.png,qualityofservice-1.png,qualityofservice-2.png,qualityofservice-3.png,qualityofservice-4.png,radiator.png,rain.png,receiver.png,receiver-off.png,receiver-on.png,recorder.png,returnpipe.png,rgb.png,rollershutter.png,rollershutter-0.png,rollershutter-10.png,rollershutter-20.png,rollershutter-30.png,rollershutter-40.png,rollershutter-50.png,rollershutter-60.png,rollershutter-70.png,rollershutter-80.png,rollershutter-90.png,rollershutter-100.png,screen.png,screen-off.png,screen-on.png,settings.png,sewerage.png,sewerage-0.png,sewerage-10.png,sewerage-20.png,sewerage-30.png,sewerage-40.png,sewerage-50.png,sewerage-60.png,sewerage-70.png,sewerage-80.png,sewerage-90.png,sewerage-100.png,shield.png,shield-0.png,shield-1.png,siren.png,siren-off.png,siren-on.png,slider.png,slider-0.png,slider-10.png,slider-20.png,slider-30.png,slider-40.png,slider-50.png,slider-60.png,slider-70.png,slider-80.png,slider-90.png,slider-100.png,smiley.png,smoke.png,snow.png,sofa.png,softener.png,solarplant.png,soundvolume.png,soundvolume-0.png,soundvolume-33.png,soundvolume-66.png,soundvolume-100.png,soundvolume_mute.png,status.png,suitcase.png,sun.png,sunrise.png,sunset.png,sun_clouds.png,switch.png,switch-off.png,switch-on.png,temperature.png,temperature_cold.png,temperature_hot.png,terrace.png,text.png,time.png,time-on.png,toilet.png,vacation.png,video.png,wallswitch.png,wallswitch-off.png,wallswitch-on.png,wardrobe.png,washingmachine.png,washingmachine_2.png,washingmachine_2-0.png,washingmachine_2-1.png,washingmachine_2-2.png,washingmachine_2-3.png,water.png,whitegood.png,wind.png,window.png,window-ajar.png,window-closed.png,window-open.png,woman_1.png,woman_2.png,woman_3.png,woman_4.png,woman_5.png,woman_6.png,zoom.png," %}
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

