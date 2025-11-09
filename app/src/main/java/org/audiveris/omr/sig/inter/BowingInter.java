//------------------------------------------------------------------------------------------------//
//                                                                                                //
//                                      B o w i n g I n t e r                                     //
//                                                                                                //
//------------------------------------------------------------------------------------------------//
// <editor-fold defaultstate="collapsed" desc="hdr">
//
//  Copyright © Audiveris 2025. All rights reserved.
//
//  This program is free software: you can redistribute it and/or modify it under the terms of the
//  GNU Affero General Public License as published by the Free Software Foundation, either version
//  3 of the License, or (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
//  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//  See the GNU Affero General Public License for more details.
//
//  You should have received a copy of the GNU Affero General Public License along with this
//  program.  If not, see <http://www.gnu.org/licenses/>.
//------------------------------------------------------------------------------------------------//
// </editor-fold>
package org.audiveris.omr.sig.inter;

import org.audiveris.omr.glyph.Glyph;
import org.audiveris.omr.glyph.Shape;
import org.audiveris.omr.sheet.Scale;
import org.audiveris.omr.sheet.Staff;
import org.audiveris.omr.sheet.SystemInfo;
import org.audiveris.omr.sheet.rhythm.Voice;
import org.audiveris.omr.sig.relation.HeadBowingRelation;
import org.audiveris.omr.sig.relation.Link;
import org.audiveris.omr.sig.relation.Relation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class <code>BowingInter</code> represents a bowing indication (upbow or downbow)
 * for string instruments.
 *
 * @author Hervé Bitteur
 */
@XmlRootElement(name = "bowing")
public class BowingInter
        extends AbstractInter
{
    //~ Static fields/initializers -----------------------------------------------------------------

    private static final Logger logger = LoggerFactory.getLogger(BowingInter.class);

    //~ Constructors -------------------------------------------------------------------------------

    /**
     * No-argument constructor needed for JAXB.
     */
    private BowingInter ()
    {
    }

    /**
     * Creates a new <code>BowingInter</code> object.
     *
     * @param glyph underlying glyph
     * @param shape precise shape (UPBOW or DOWNBOW)
     * @param grade evaluation value
     */
    public BowingInter (Glyph glyph,
                        Shape shape,
                        Double grade)
    {
        super(glyph, (glyph != null) ? glyph.getBounds() : null, shape, grade);
    }

    //~ Methods ------------------------------------------------------------------------------------

    //-------//
    // added //
    //-------//
    @Override
    public void added ()
    {
        super.added();

        setAbnormal(true); // No head linked yet
    }

    //---------------//
    // checkAbnormal //
    //---------------//
    @Override
    public boolean checkAbnormal ()
    {
        // Check if a note head is connected
        setAbnormal(!sig.hasRelation(this, HeadBowingRelation.class));

        return isAbnormal();
    }

    //----------//
    // getStaff //
    //----------//
    @Override
    public Staff getStaff ()
    {
        if (staff == null) {
            for (Relation rel : sig.getRelations(this, HeadBowingRelation.class)) {
                final HeadInter head = (HeadInter) sig.getOppositeInter(this, rel);

                return staff = head.getStaff();
            }
        }

        return staff;
    }

    //----------//
    // getVoice //
    //----------//
    @Override
    public Voice getVoice ()
    {
        for (Relation rel : sig.getRelations(this, HeadBowingRelation.class)) {
            return sig.getOppositeInter(this, rel).getVoice();
        }

        return null;
    }

    //------------//
    // lookupLink //
    //------------//
    /**
     * Try to detect a link between this bowing instance and a head in a HeadChord nearby.
     *
     * @param systemHeadChords abscissa-ordered collection of head chords in system
     * @param profile          desired profile level
     * @return the link found or null
     */
    protected Link lookupLink (List<Inter> systemHeadChords,
                               int profile)
    {
        if (systemHeadChords.isEmpty()) {
            return null;
        }

        final SystemInfo system = systemHeadChords.get(0).getSig().getSystem();
        final Scale scale = system.getSheet().getScale();
        final HeadInter head = TechnicalHelper.lookupHead(
                this,
                systemHeadChords,
                scale.toPixels(HeadBowingRelation.getXOutGapMaximum(profile)),
                scale.toPixels(HeadBowingRelation.getYGapMaximum(profile)));

        if (head == null) {
            return null;
        }

        return new Link(head, new HeadBowingRelation(), false);
    }

    //-------------//
    // searchLinks //
    //-------------//
    @Override
    public Collection<Link> searchLinks (SystemInfo system)
    {
        final int profile = Math.max(getProfile(), system.getProfile());
        final List<Inter> systemHeadChords = system.getSig().inters(HeadChordInter.class);
        Collections.sort(systemHeadChords, Inters.byAbscissa);

        Link link = lookupLink(systemHeadChords, profile);

        return (link == null) ? Collections.emptyList() : Collections.singleton(link);
    }

    //---------------//
    // searchUnlinks //
    //---------------//
    @Override
    public Collection<Link> searchUnlinks (SystemInfo system,
                                           Collection<Link> links)
    {
        return searchObsoletelinks(links, HeadBowingRelation.class);
    }

    //~ Static Methods -----------------------------------------------------------------------------

    //------------------//
    // createValidAdded //
    //------------------//
    /**
     * (Try to) create and add a valid BowingInter.
     *
     * @param glyph            underlying glyph
     * @param shape            detected shape (UPBOW or DOWNBOW)
     * @param grade            assigned grade
     * @param system           containing system
     * @param systemHeadChords system head chords, ordered by abscissa
     * @return the created bowing or null
     */
    public static BowingInter createValidAdded (Glyph glyph,
                                                Shape shape,
                                                double grade,
                                                SystemInfo system,
                                                List<Inter> systemHeadChords)
    {
        if (glyph.isVip()) {
            logger.info("VIP BowingInter create {} as {}", glyph, shape);
        }

        BowingInter bowing = new BowingInter(glyph, shape, grade);
        Link link = bowing.lookupLink(systemHeadChords, system.getProfile());

        if (link != null) {
            system.getSig().addVertex(bowing);
            link.applyTo(bowing);

            return bowing;
        }

        return null;
    }
}
