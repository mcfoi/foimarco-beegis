/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package eu.hydrologis.jgrass.gpsnmea.gps;

import java.awt.Color;

/**
 * The GPS preferences at runtime. 
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GpsProperties {
    
    public static int gpsSymbolWidth = 1;
    
    public static Color gpsActiveColor = Color.red;
    
    public static Color gpsNonActiveColor = Color.magenta;
    
    public static boolean doCrosshair = false;

    public static int crosshairWidth = 1;
    
    public static Color crosshairColor = Color.gray;
    
    public static double deltaX = 0d;
    
    public static double deltaY = 0d;
}
