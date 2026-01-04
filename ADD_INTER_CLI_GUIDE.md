# CLI Guide: Adding Inters to Book Files

## Overview

The `-add-inter` CLI option allows you to programmatically add musical interpretations (inters) to Audiveris book files from the command line. This is useful for:
- Automated music notation editing
- Batch processing
- Testing and debugging
- Integration with external tools

## Syntax

```bash
audiveris -add-inter <shape> <x> <y> [options] <book-file.omr>
```

### Required Arguments

1. **`<shape>`** - The type of inter to create (Shape enum value)
   - Examples: `NOTEHEAD_BLACK`, `G_CLEF`, `ACCENT`, `FLAT`, `FERMATA`
   - Case-insensitive (e.g., `notehead_black` works too)

2. **`<x>`** - X coordinate in pixels (integer)
   - Horizontal position on the sheet

3. **`<y>`** - Y coordinate in pixels (integer)
   - Vertical position on the sheet

4. **`<book-file.omr>`** - Path to the Audiveris book file

### Optional Flags

- `-batch` - Run without GUI (recommended for CLI usage)
- `-save` - Save the book file after adding the inter
- `-export` - Export to MusicXML after modification
- `-sheets <IDs>` - Target specific sheet numbers (default: first valid sheet)

## Examples

### 1. Add a Black Notehead

```bash
java -jar build/libs/audiveris.jar \
  -add-inter NOTEHEAD_BLACK 500 300 \
  -batch -save \
  myscore.omr
```

### 2. Add a G Clef

```bash
java -jar build/libs/audiveris.jar \
  -add-inter G_CLEF 100 200 \
  -batch -save \
  piece.omr
```

### 3. Add an Accent and Export

```bash
java -jar build/libs/audiveris.jar \
  -add-inter ACCENT 450 280 \
  -batch -save -export \
  symphony.omr
```

### 4. Add to Specific Sheet

```bash
java -jar build/libs/audiveris.jar \
  -add-inter FERMATA 600 250 \
  -sheets 2 \
  -batch -save \
  multipage.omr
```

## Available Shapes

### Note Heads
- `NOTEHEAD_BLACK`
- `NOTEHEAD_VOID`
- `NOTEHEAD_WHOLE`
- `NOTEHEAD_BLACK_SMALL`
- `NOTEHEAD_VOID_SMALL`

### Clefs
- `G_CLEF`
- `G_CLEF_SMALL`
- `G_CLEF_8VA`
- `G_CLEF_8VB`
- `F_CLEF`
- `F_CLEF_SMALL`
- `F_CLEF_8VA`
- `F_CLEF_8VB`
- `C_CLEF`

### Accidentals
- `FLAT`
- `NATURAL`
- `SHARP`
- `DOUBLE_SHARP`
- `DOUBLE_FLAT`

### Articulations
- `ACCENT`
- `TENUTO`
- `STACCATO`
- `STACCATISSIMO`
- `STRONG_ACCENT`

### Dynamics
- `DYNAMICS_P`
- `DYNAMICS_PP`
- `DYNAMICS_MP`
- `DYNAMICS_F`
- `DYNAMICS_FF`
- `DYNAMICS_MF`
- `DYNAMICS_FP`
- `DYNAMICS_SF`
- `DYNAMICS_SFZ`

### Ornaments
- `GRACE_NOTE_SLASH`
- `GRACE_NOTE`
- `TR`
- `TURN`
- `TURN_INVERTED`
- `TURN_UP`
- `TURN_SLASH`
- `MORDENT`
- `MORDENT_INVERTED`

### Symbols
- `FERMATA`
- `FERMATA_BELOW`
- `FERMATA_DOT`
- `CAESURA`
- `BREATH_MARK`
- `ARPEGGIATO`

### Rests
- `WHOLE_REST`
- `HALF_REST`
- `QUARTER_REST`
- `EIGHTH_REST`
- `ONE_16TH_REST`
- `ONE_32ND_REST`
- `ONE_64TH_REST`
- `ONE_128TH_REST`

For a complete list, see: `app/src/main/java/org/audiveris/omr/glyph/Shape.java`

## How It Works

### 1. Position Calculation
The system automatically:
- Finds the closest staff to the specified (x, y) coordinates
- Derives the inter's exact position based on the shape's symbol template
- Applies **automatic snapping** to staff lines, stems, or other relevant structures

### 2. Structure Maintenance
The system ensures:
- Proper staff assignment (closest staff is used)
- System assignment (derived from staff)
- Font selection (based on staff interline and music family)
- Bounds calculation (symbol dimensions applied at location)
- **Automatic ID assignment** (unique ID generated)
- **Relation discovery** (finds and creates links to nearby compatible inters)
- **SIG integration** (adds to Semantic Interpretation Graph properly)

### 3. Validation
The system validates:
- ✓ Shape exists in Shape enum
- ✓ Coordinates are valid integers
- ✓ Book file exists and is readable
- ✓ Staff is found at the specified location
- ✓ Inter can be created for the given shape

## Error Handling

### Common Errors

1. **"No staff found at location"**
   - Solution: Ensure coordinates are within the sheet boundaries and near a staff

2. **"Cannot create inter for shape"**
   - Solution: Verify the shape name is correct and supported for manual creation

3. **"No valid sheet found"**
   - Solution: Make sure the book file has at least one valid sheet

4. **"Invalid shape"**
   - Solution: Check the shape name against the Shape enum (case-insensitive)

## Integration Examples

### Bash Script

```bash
#!/bin/bash
# Batch add multiple inters

BOOK="myscore.omr"

# Add notes
java -jar audiveris.jar -add-inter NOTEHEAD_BLACK 500 300 -batch -save "$BOOK"
java -jar audiveris.jar -add-inter NOTEHEAD_BLACK 550 280 -batch -save "$BOOK"
java -jar audiveris.jar -add-inter NOTEHEAD_BLACK 600 300 -batch -save "$BOOK"

# Add dynamics
java -jar audiveris.jar -add-inter DYNAMICS_F 450 400 -batch -save "$BOOK"

# Export final result
java -jar audiveris.jar -export -batch "$BOOK"
```

### Python Script

```python
import subprocess

def add_inter(book_file, shape, x, y, save=True):
    cmd = [
        "java", "-jar", "build/libs/audiveris.jar",
        "-add-inter", shape, str(x), str(y),
        "-batch"
    ]
    if save:
        cmd.append("-save")
    cmd.append(book_file)

    subprocess.run(cmd, check=True)

# Usage
add_inter("myscore.omr", "NOTEHEAD_BLACK", 500, 300)
add_inter("myscore.omr", "G_CLEF", 100, 200)
add_inter("myscore.omr", "ACCENT", 450, 280)
```

## Logging

The CLI provides detailed logging information:

```
INFO: Adding inter: shape=NOTEHEAD_BLACK, x=500, y=300
INFO: Successfully added NOTEHEAD_BLACK (ID=1234) at (500, 300) in sheet #1
```

Error messages are also logged:
```
WARN: No staff found at location (999, 999)
WARN: Cannot create inter for shape: INVALID_SHAPE
```

## Building the Project

```bash
# Compile only
./gradlew compileJava

# Build complete JAR
./gradlew build

# Skip tests for faster builds
./gradlew build -x test
```

## Testing

Use the provided test script:

```bash
./test-add-inter.sh
```

Or run manual tests:

```bash
# Display help
java -jar build/libs/audiveris.jar -help

# Test adding an inter
java -jar build/libs/audiveris.jar \
  -add-inter NOTEHEAD_BLACK 500 300 \
  -batch -save \
  test.omr

# Verify in GUI
java -jar build/libs/audiveris.jar test.omr
```

## Implementation Details

### Code Location
- Main implementation: `app/src/main/java/org/audiveris/omr/CLI.java`
- Classes added:
  - `AddInterParams` - Parameter holder
  - `AddInterOptionHandler` - Args4j option handler
  - `addInterToBook()` - Core implementation method

### Dependencies
- `InterFactory.createManual()` - Creates the inter instance
- `StaffManager.getClosestStaff()` - Finds nearest staff
- `Inter.deriveFrom()` - Calculates position and bounds
- `SIGraph.addVertex()` - Registers inter and assigns ID
- `InterController.addInter()` - Adds with proper structure

## Troubleshooting

### Build Issues
```bash
# Clean build
./gradlew clean build

# Check Java version (requires Java 24+)
java -version
```

### Runtime Issues
```bash
# Enable debug logging
java -Dlog.level=DEBUG -jar build/libs/audiveris.jar -add-inter ...

# Check book file is valid
java -jar build/libs/audiveris.jar -batch book.omr
```

## Future Enhancements

Potential improvements:
- Support for JSON batch file input
- Interactive mode to show available shapes
- Dry-run mode to validate without modifying
- Return inter ID for scripting use
- Support for specifying staff/system explicitly

## Related Documentation

- [Shape Enum](app/src/main/java/org/audiveris/omr/glyph/Shape.java)
- [InterFactory](app/src/main/java/org/audiveris/omr/sheet/symbol/InterFactory.java)
- [CLI Class](app/src/main/java/org/audiveris/omr/CLI.java)

## Support

For issues or questions:
- GitHub Issues: https://github.com/Audiveris/audiveris/issues
- Documentation: https://audiveris.github.io/audiveris/

---

**Generated with Claude Code** - Implementation completed 2026-01-01
