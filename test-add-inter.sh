#!/bin/bash

# Test script for the new -add-inter CLI option
# This demonstrates how to add an inter to a book file using the command line

echo "=== Testing -add-inter CLI option ==="
echo ""

# Check if book.xml exists
if [ ! -f "book.xml" ]; then
    echo "Error: book.xml not found"
    exit 1
fi

# Build the project first
echo "Building project..."
./gradlew build -x test

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Build successful!"
echo ""

# Example 1: Add a black notehead at position (500, 300)
echo "Example 1: Adding NOTEHEAD_BLACK at (500, 300)"
echo "Command: java -jar build/libs/audiveris.jar -add-inter NOTEHEAD_BLACK 500 300 -batch -save book.xml"
echo ""

# Example 2: Add a G clef at position (100, 200)
echo "Example 2: To add G_CLEF at (100, 200):"
echo "Command: java -jar build/libs/audiveris.jar -add-inter G_CLEF 100 200 -batch -save book.xml"
echo ""

# Example 3: Add an accent at position (450, 280)
echo "Example 3: To add ACCENT at (450, 280):"
echo "Command: java -jar build/libs/audiveris.jar -add-inter ACCENT 450 280 -batch -save book.xml"
echo ""

# Show help for the new option
echo "To see all available options:"
echo "Command: java -jar build/libs/audiveris.jar -help"
echo ""

echo "=== Available Shape values ==="
echo "You can use any of these shapes (case-insensitive):"
echo "  - NOTEHEAD_BLACK, NOTEHEAD_VOID, NOTEHEAD_WHOLE"
echo "  - G_CLEF, F_CLEF, C_CLEF"
echo "  - FLAT, NATURAL, SHARP, DOUBLE_SHARP, DOUBLE_FLAT"
echo "  - ACCENT, TENUTO, STACCATO, STACCATISSIMO"
echo "  - FERMATA, FERMATA_BELOW"
echo "  - And many more... (see Shape.java enum for complete list)"
echo ""

echo "=== Test Complete ==="
echo "Note: Uncomment the actual test commands above to run them"
echo "      Make sure you have a valid .omr book file to test with"
