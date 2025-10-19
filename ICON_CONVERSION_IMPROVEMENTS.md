# Icon Conversion Script Improvements

## Issues Fixed

### 1. Missing Stroke Attributes

**Problem:** Icons with `stroke` attributes (like `userline`, `calendaradd`, etc.) were not getting their stroke details in the XML drawable files. The script was only capturing direct path attributes but missing group-level attributes.

**Solution:**

- Added support for parsing `<g>` (group) elements that contain stroke attributes
- Implemented attribute inheritance from parent groups to child paths
- Paths now properly inherit stroke properties like `stroke`, `stroke-width`, `stroke-linecap`, etc.

### 2. Improved Attribute Extraction

**Changes:**

- Created `extract_attributes()` helper function that processes both element-level and parent group attributes
- Attributes from parent groups are inherited by child elements
- Prevents duplicate path processing using a `processed_paths` set

### 3. Better Stroke vs Fill Handling

**Improvements:**

- Stroke attributes are now prioritized for line-style icons
- Proper handling of `fill="none"` - now converts to transparent fill with visible stroke
- Better logic for determining when to use stroke vs fill:
  - If stroke is present → use transparent fill (unless explicitly filled)
  - If only fill is present → use solid fill
  - If neither is present → default to black fill

### 4. Enhanced Color Conversion

**Updates:**

- Modified `svg_color_to_android()` to accept a default parameter
- Better handling of `currentColor` (converts to black)
- Proper support for `none` color (converts to transparent)

### 5. Proper Opacity Handling

**Fixed:**

- Group-level `opacity` attribute now properly applied to child paths
- Separate handling of `fill-opacity` and `stroke-opacity`
- General `opacity` applies to both fill and stroke when specific values aren't set

## Example: Calendar Add Icon

### Before (Missing Stroke)

```xml
<path
    android:pathData="M2 12c0-3.771..."
    android:fillColor="@android:color/black" />
```

### After (With Proper Stroke)

```xml
<path
    android:pathData="M2 12c0-3.771..."
    android:strokeColor="@android:color/black"
    android:strokeWidth="1.5"
    android:fillColor="@android:color/transparent" />
```

## Results

- All 75 SVG files successfully converted
- Line icons now display properly with stroke rendering
- Duotone effects preserved with opacity attributes
- Better visual fidelity to original SVG designs

## Files Modified

- `icon.py` - Complete rewrite of path parsing and XML generation logic
