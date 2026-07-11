# Config GUI

The config GUI is the main selling point of SoftConfig. It can be manually constructed using a [`ConfigStructureReader` subclass](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.processor/-config-structure-reader/index.html),
or by using [`ConfigProcessorDriver`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.processor/-config-processor-driver/index.html) or the [`ManagedConfig` (for full automation of config saving and displaying)](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.managed/-managed-config/index.html)

## Structure

The config structure is roughly mapped to Java classes. Each category, subcategory and accordion is its own Java object.

The base class needs to extend [`Config`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig/-config/index.html) and each field needs to be non-static. This way your config is also easily
serializable as a Json Object. MoulConfig is however completely agnostic towards your configs save format. The only
requirement is that instances do not get reassigned. So updating the config object after you have processed a config
requires that config to be reprocessed (and old MoulConfig to be discarded).

If you specifically do want MoulConfig to load and save files for you, consider obtaining the instance of your config
through [`ManagedConfig`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.managed/-managed-config/index.html)

### Top Level Structure

You can specify categories
using [`@Category`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.annotations/-category/index.html).
You can even nest categories once to create subcategories. Subsubcategories however do not work.

```java
public class MyConfig extends Config {
    @Override
    public String getTitle() {
        return "§bMyMod Config";
    }

    @Category(name = "Category Name", desc = "Category Description")
    public MyCategory myCategory = new MyCategory();

    public static class MyCategory {
        @Category(name = "SubCategory", desc = "Sub category description")
        public MySubCategory subCategory = new MySubCategory();
    }
}
```

Note that even tho i sometimes use static inner classes, you can have any sort of class (even including non-static
inner classes) as your structure.

### Inside each category

Inside each category you can specify config options
using [`@ConfigOption`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.annotations/-config-option/index.html).

In addition to the config option which contains meta information like the name, you will need to add another annotation
of your choice to specify an editor for that variable. Check out all
the [annotations](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.annotations/index.html).

Make sure that you check the Javadoc on each annotation to know which type your field needs to have for it to work.

```java
public class MySubCategory {
    @ConfigOption(name = "Text Test", desc = "Text Editor Test")
    @ConfigEditorText
    public String text = "Text";

    @ConfigOption(name = "Number", desc = "Slider test")
    @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
    public int slider = 0;


    @ConfigOption(name = "Key Binding", desc = "Key Binding")
    @ConfigEditorKeybind(defaultKey = Keyboard.KEY_F)
    public int keyBoard = Keyboard.KEY_F;
}
```

### Conditional visibility

Use [`@ConfigVisibleIf`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.annotations/-config-visible-if/index.html)
when one option only matters if another boolean option is enabled. The dependent option is hidden while the condition is
not met, and it slides open or closed when the controlling option changes.

For example, an existing Kotlin config might look like this:

```kotlin
@field:Expose
@field:ConfigOption(name = "Show Progress", desc = "Show progress on screen.")
@field:ConfigEditorBoolean
var showProgress = true

@field:Expose
@field:ConfigOption(name = "Progress Position", desc = "Where the progress display appears.")
@field:ConfigEditorDropdown
var progressPosition = ProgressPosition.RIGHT
```

To hide `progressPosition` unless `showProgress` is enabled, add `@field:ConfigVisibleIf("showProgress")`:

```kotlin
@field:Expose
@field:ConfigOption(name = "Show Progress", desc = "Show progress on screen.")
@field:ConfigEditorBoolean
var showProgress = true

@field:Expose
@field:ConfigOption(name = "Progress Position", desc = "Where the progress display appears.")
@field:ConfigVisibleIf("showProgress")
@field:ConfigEditorDropdown
var progressPosition = ProgressPosition.RIGHT
```

The string is the field name of the boolean option, not the display name. The controlling field must be in the same
config object as the dependent option. You can also invert the condition, so the option only appears when the toggle is
off:

```kotlin
@field:ConfigVisibleIf(value = "showProgress", expected = false)
```

### Accordions

Sometimes just subcategories are not enough and you will want to group your options even
further. [`@Accordion`s](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.annotations/-accordion/index.html)
allow you to nest options arbitrarily deep.

```java
public class MySubCategory {
    @ConfigOption(name = "Text Test", desc = "Text Editor Test")
    @ConfigEditorText
    public String text = "Text";

    @Accordion
    @ConfigOption(name = "Hehe", desc = "hoho")
    public MyAccordion myAccordion = new MyAccordion();

    public static class MyAccordion {
        @ConfigOption(name = "Number", desc = "Slider test")
        @ConfigEditorSlider(minValue = 0, maxValue = 10, minStep = 1)
        public int slider = 0;


        @ConfigOption(name = "Key Binding", desc = "Key Binding")
        @ConfigEditorKeybind(defaultKey = Keyboard.KEY_F)
        public int keyBoard = Keyboard.KEY_F;
    }
}
```

### Properties

Sometimes you want to listen to changes to a config variable and run some updates based on that. For that you can use
[`Property<T>`](https://akinsoft.github.io/SoftConfig/javadocs/common/io.github.notenoughupdates.moulconfig.observer/-property/index.html). Check the Javadoc for
more information on how to use Properties.

