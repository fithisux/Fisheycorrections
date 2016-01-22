/*   Fisheycorrections: A Java(TM) based fishey distrotion correction software.
 *   
 * 
 *   Fisheycorrections is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Fisheycorrections is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *   You should have received a copy of the GNU General Public License
 *   along with Fisheycorrections.  If not, see <http://www.gnu.org/licenses/>.
 */
package fisheycorrect;

import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.image.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

/**
 *
 * @author FITHIS
 */
public class testCorrection {

    public static void main(String[] args) {
        BufferedImage inputImage = null;
        try {
            inputImage = ImageIO.read(new File("c:/workspace/cleared/cam6.bmp"));
        } catch (IOException e) {
        }

        CameraModel inModel = FisheyCorrection.toCameraModel(inputImage);
        FisheyCorrection testme = new FisheyCorrection(inModel, new CameraUtils.EquidistantDistortion(CameraUtils.GoProParams.focal_length));

        int width = (int) Math.round(inputImage.getWidth());
        int height = (int) Math.round(inputImage.getHeight());

        CameraModel outModel = testme.resample(new Point(width, height));
        try {
            ImageIO.write(outModel.image, "bmp", new File("c:/workspace/cleared/output6.bmp"));
        } catch (IOException e) {
        }

        try {
            ImageIO.write(outModel.mask, "bmp", new File("c:/workspace/cleared/roi6.bmp"));
        } catch (IOException e) {
        }

        try {
            ImageIO.write(outModel.cropCorrected(), "bmp", new File("c:/workspace/cleared/sub6.bmp"));
        } catch (IOException e) {
        }
    }

}
