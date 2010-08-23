/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Library General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Library General Public License
 * along with this library; if not, write to the Free Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package eu.hydrologis.jgrass.featureeditor.xml.annotated;

import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.DEFAULT;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.FIELDNAME;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.HEIGHTFE;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.HEIGHTHINT;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.LIST;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.NAME;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.ORIENTATION;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.RADIOBUTTON;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.TEXT;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.WIDTHFE;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.WIDTHHINT;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.XFE;
import static eu.hydrologis.jgrass.featureeditor.xml.annotated.AnnotationConstants.YFE;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class representing an swt combobox.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@XmlRootElement(name = RADIOBUTTON)
public class RadioButton {

    /**
     * The attribute's table field name.
     */
    @XmlAttribute(name = FIELDNAME)
    public final String fieldName = null;

    /**
     * Unique name for the object.
     */
    @XmlAttribute(name = NAME)
    public String name = null;

    /**
     * Text for the radiobutton label.
     */
    @XmlAttribute(name = TEXT)
    public String text = null;

    /**
     * The list of items out of which to make radiobuttons.
     */
    @XmlAttribute(name = LIST)
    public List<String> list = null;

    /**
     * A default item of the list of radiobuttons to be selected.
     */
    @XmlAttribute(name = DEFAULT)
    public String defaultText = null;

    /**
     * Orientation of the radiobuttons (horizontal, vertical).
     */
    @XmlAttribute(name = ORIENTATION)
    public String orientation = null;
    
    /**
     * A hint for the width in the gui. 
     */
    @XmlAttribute(name = WIDTHHINT)
    public Integer widthHint;

    /**
     * A hint for the height in the gui. 
     */
    @XmlAttribute(name = HEIGHTHINT)
    public Integer heightHint;

    /**
     * The x position of the widget in the Form Editor. Doesn't affect gui.
     */
    @XmlAttribute(name = XFE)
    public Integer xFE;

    /**
     * The y position of the widget in the Form Editor. Doesn't affect gui.
     */
    @XmlAttribute(name = YFE)
    public Integer yFE;

    /**
     * The width of the widget in the Form Editor. Doesn't affect gui.
     */
    @XmlAttribute(name = WIDTHFE)
    public Integer widthFE;

    /**
     * The height of the widget in the Form Editor. Doesn't affect gui.
     */
    @XmlAttribute(name = HEIGHTFE)
    public Integer heightFE;

}