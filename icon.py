#!/usr/bin/env python3
"""
SVG to Android Vector Drawable Converter
Converts all SVG files from images/ to Android drawable XMLs
"""

from __future__ import annotations
import re
import xml.etree.ElementTree as ET
from pathlib import Path

def parse_svg_path_data(svg_file: Path) -> tuple[list[dict[str, str]], str, str, str]:
    """Parse SVG file and extract path data"""
    try:
        tree = ET.parse(svg_file)
        root = tree.getroot()
        
        # Remove namespace if present (not used but kept for clarity)
        # ns = {'svg': 'http://www.w3.org/2000/svg'}
        
        paths: list[dict[str, str]] = []
        
        # Find all path elements
        for path in root.iter('{http://www.w3.org/2000/svg}path'):
            path_data: dict[str, str] = {}
            if 'd' in path.attrib:
                path_data['pathData'] = path.attrib['d']
            if 'fill' in path.attrib:
                path_data['fillColor'] = path.attrib['fill']
            if 'stroke' in path.attrib:
                path_data['strokeColor'] = path.attrib['stroke']
            if 'stroke-width' in path.attrib:
                path_data['strokeWidth'] = path.attrib['stroke-width']
            if 'opacity' in path.attrib:
                path_data['alpha'] = path.attrib['opacity']
            if 'fill-opacity' in path.attrib:
                path_data['fillAlpha'] = path.attrib['fill-opacity']
            if 'stroke-opacity' in path.attrib:
                path_data['strokeAlpha'] = path.attrib['stroke-opacity']
            if 'stroke-linecap' in path.attrib:
                path_data['strokeLineCap'] = path.attrib['stroke-linecap']
            if 'stroke-linejoin' in path.attrib:
                path_data['strokeLineJoin'] = path.attrib['stroke-linejoin']
            
            paths.append(path_data)
        
        # Also check for paths without namespace
        for path in root.iter('path'):
            path_data: dict[str, str] = {}
            if 'd' in path.attrib:
                path_data['pathData'] = path.attrib['d']
            if 'fill' in path.attrib:
                path_data['fillColor'] = path.attrib['fill']
            if 'stroke' in path.attrib:
                path_data['strokeColor'] = path.attrib['stroke']
            if 'stroke-width' in path.attrib:
                path_data['strokeWidth'] = path.attrib['stroke-width']
            if 'opacity' in path.attrib:
                path_data['alpha'] = path.attrib['opacity']
            if 'fill-opacity' in path.attrib:
                path_data['fillAlpha'] = path.attrib['fill-opacity']
            if 'stroke-opacity' in path.attrib:
                path_data['strokeAlpha'] = path.attrib['stroke-opacity']
            if 'stroke-linecap' in path.attrib:
                path_data['strokeLineCap'] = path.attrib['stroke-linecap']
            if 'stroke-linejoin' in path.attrib:
                path_data['strokeLineJoin'] = path.attrib['stroke-linejoin']
            
            if path_data and 'pathData' in path_data:
                paths.append(path_data)
        
        # Find ellipse elements
        for ellipse in root.iter('{http://www.w3.org/2000/svg}ellipse'):
            path_data: dict[str, str] = {}
            cx = float(ellipse.attrib.get('cx', 0))
            cy = float(ellipse.attrib.get('cy', 0))
            rx = float(ellipse.attrib.get('rx', 0))
            ry = float(ellipse.attrib.get('ry', 0))
            path_data['pathData'] = f'M {cx} {cy} m -{rx} 0 a {rx} {ry} 0 1 1 {rx*2} 0 a {rx} {ry} 0 1 1 -{rx*2} 0'
            if 'fill' in ellipse.attrib:
                path_data['fillColor'] = ellipse.attrib['fill']
            paths.append(path_data)
        
        for ellipse in root.iter('ellipse'):
            path_data: dict[str, str] = {}
            cx = float(ellipse.attrib.get('cx', 0))
            cy = float(ellipse.attrib.get('cy', 0))
            rx = float(ellipse.attrib.get('rx', 0))
            ry = float(ellipse.attrib.get('ry', 0))
            path_data['pathData'] = f'M {cx} {cy} m -{rx} 0 a {rx} {ry} 0 1 1 {rx*2} 0 a {rx} {ry} 0 1 1 -{rx*2} 0'
            if 'fill' in ellipse.attrib:
                path_data['fillColor'] = ellipse.attrib['fill']
            if path_data and 'pathData' in path_data:
                paths.append(path_data)
        
        # Find circle elements
        for circle in root.iter('{http://www.w3.org/2000/svg}circle'):
            path_data: dict[str, str] = {}
            cx = float(circle.attrib.get('cx', 0))
            cy = float(circle.attrib.get('cy', 0))
            r = float(circle.attrib.get('r', 0))
            # Convert circle to ellipse path
            path_data['pathData'] = f'M {cx} {cy} m -{r} 0 a {r} {r} 0 1 1 {r*2} 0 a {r} {r} 0 1 1 -{r*2} 0'
            if 'fill' in circle.attrib:
                path_data['fillColor'] = circle.attrib['fill']
            if 'stroke' in circle.attrib:
                path_data['strokeColor'] = circle.attrib['stroke']
            if 'stroke-width' in circle.attrib:
                path_data['strokeWidth'] = circle.attrib['stroke-width']
            paths.append(path_data)
        
        for circle in root.iter('circle'):
            path_data: dict[str, str] = {}
            cx = float(circle.attrib.get('cx', 0))
            cy = float(circle.attrib.get('cy', 0))
            r = float(circle.attrib.get('r', 0))
            # Convert circle to ellipse path
            path_data['pathData'] = f'M {cx} {cy} m -{r} 0 a {r} {r} 0 1 1 {r*2} 0 a {r} {r} 0 1 1 -{r*2} 0'
            if 'fill' in circle.attrib:
                path_data['fillColor'] = circle.attrib['fill']
            if 'stroke' in circle.attrib:
                path_data['strokeColor'] = circle.attrib['stroke']
            if 'stroke-width' in circle.attrib:
                path_data['strokeWidth'] = circle.attrib['stroke-width']
            if path_data and 'pathData' in path_data:
                paths.append(path_data)
        
        # Get viewBox for dimensions
        viewbox = root.attrib.get('viewBox', '0 0 24 24')
        width = root.attrib.get('width', '24')
        height = root.attrib.get('height', '24')
        
        return paths, viewbox, width, height
    
    except Exception as e:
        print(f"Error parsing {svg_file}: {e}")
        return [], '0 0 24 24', '24', '24'

def svg_color_to_android(color: str | None) -> str:
    """Convert SVG color to Android color format"""
    if not color or color == 'none':
        return '@android:color/transparent'
    if color == 'currentColor':
        return '@android:color/black'
    if color.startswith('#'):
        return color
    if color == 'black' or color == '#000' or color == '#000000':
        return '@android:color/black'
    if color == 'white' or color == '#fff' or color == '#ffffff':
        return '@android:color/white'
    return color

def create_vector_drawable(svg_file: Path, output_file: Path) -> bool:
    """Convert SVG to Android Vector Drawable XML"""
    paths, viewbox, _width, _height = parse_svg_path_data(svg_file)
    
    if not paths:
        print(f"Warning: No paths found in {svg_file}")
        return False
    
    # Parse viewBox
    vb_parts = viewbox.split()
    if len(vb_parts) == 4:
        vb_width = vb_parts[2]
        vb_height = vb_parts[3]
    else:
        vb_width = '24'
        vb_height = '24'
    
    # Create XML structure
    xml_lines = ['<vector xmlns:android="http://schemas.android.com/apk/res/android"']
    xml_lines.append('    android:width="24dp"')
    xml_lines.append('    android:height="24dp"')
    xml_lines.append(f'    android:viewportWidth="{vb_width}"')
    xml_lines.append(f'    android:viewportHeight="{vb_height}">')
    
    for path_data in paths:
        if 'pathData' not in path_data:
            continue
            
        xml_lines.append('    <path')
        xml_lines.append(f'        android:pathData="{path_data["pathData"]}"')
        
        if 'strokeWidth' in path_data:
            xml_lines.append(f'        android:strokeWidth="{path_data["strokeWidth"]}"')
        
        if 'strokeColor' in path_data:
            color = svg_color_to_android(path_data['strokeColor'])
            xml_lines.append(f'        android:strokeColor="{color}"')
        
        if 'fillColor' in path_data:
            color = svg_color_to_android(path_data['fillColor'])
            xml_lines.append(f'        android:fillColor="{color}"')
        elif 'strokeColor' not in path_data:
            # If no fill or stroke specified, default to black fill
            xml_lines.append('        android:fillColor="@android:color/black"')
        else:
            # If only stroke is specified, transparent fill
            xml_lines.append('        android:fillColor="@android:color/transparent"')
        
        if 'strokeLineCap' in path_data:
            xml_lines.append(f'        android:strokeLineCap="{path_data["strokeLineCap"]}"')
        
        if 'strokeLineJoin' in path_data:
            xml_lines.append(f'        android:strokeLineJoin="{path_data["strokeLineJoin"]}"')
        
        if 'strokeAlpha' in path_data:
            xml_lines.append(f'        android:strokeAlpha="{path_data["strokeAlpha"]}"')
        
        if 'fillAlpha' in path_data:
            xml_lines.append(f'        android:fillAlpha="{path_data["fillAlpha"]}"')
        
        if 'alpha' in path_data:
            xml_lines.append(f'        android:strokeAlpha="{path_data["alpha"]}"')
            xml_lines.append(f'        android:fillAlpha="{path_data["alpha"]}"')
        
        xml_lines.append('        />')
    
    xml_lines.append('</vector>')
    
    # Write to file
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write('\n'.join(xml_lines))
    
    return True

def convert_filename(svg_filename: str) -> str:
    """Convert SVG filename to Android drawable naming convention"""
    name = Path(svg_filename).stem
    
    # Remove 'Solar' prefix
    name = re.sub(r'^Solar', '', name)
    
    # Handle special patterns
    name = name.replace('BoldDuotone', '_bold')
    name = name.replace('LineDuotone', '_line')
    name = name.replace('LinearDuotone', '_line')
    name = name.replace('Linear', '_line')
    
    # Convert camelCase to snake_case
    name = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    name = re.sub('([a-z0-9])([A-Z])', r'\1_\2', name)
    name = name.lower()
    
    # Replace hyphens with underscores (Android resource naming requirement)
    name = name.replace('-', '_')
    
    # Clean up multiple underscores
    name = re.sub('_+', '_', name)
    name = name.strip('_')
    
    return f'ic_{name}.xml'

def main() -> None:
    # Paths
    images_dir = Path('images')
    drawable_dir = Path('app/src/main/res/drawable')
    
    # Create drawable directory if it doesn't exist
    drawable_dir.mkdir(parents=True, exist_ok=True)
    
    # Find all SVG files
    svg_files = list(images_dir.glob('*.svg'))
    
    if not svg_files:
        print("No SVG files found in images/ directory")
        return
    
    print(f"Found {len(svg_files)} SVG files")
    print(f"Converting to {drawable_dir}/\n")
    
    success_count = 0
    failed_files: list[str] = []
    
    for svg_file in svg_files:
        output_name = convert_filename(svg_file.name)
        output_path = drawable_dir / output_name
        
        print(f"Converting: {svg_file.name} -> {output_name}")
        
        if create_vector_drawable(svg_file, output_path):
            success_count += 1
        else:
            failed_files.append(svg_file.name)
    
    print(f"\n{'='*60}")
    print(f"Conversion complete!")
    print(f"Successfully converted: {success_count}/{len(svg_files)} files")
    
    if failed_files:
        print(f"\nFailed to convert:")
        for f in failed_files:
            print(f"  - {f}")

if __name__ == '__main__':
    main()
