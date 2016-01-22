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
import java.io.*;
import java.util.*;

/**
 *
 * @author FITHIS
 */
public class FisheyCorrection {

    /**
     * @param args the command line arguments
     */
    CameraModel inModel;
    double focal_length;
    CameraUtils.CameraDistortion distortion;

    public static CameraModel toCameraModel(BufferedImage inImage) {
        CameraUtils.CameraParams params = CameraUtils.GoProParams.getDefaults();
        return new CameraModel(inImage, null, params);
    }

    public FisheyCorrection(CameraModel inModel, CameraUtils.CameraDistortion distortion) {
        this.inModel = inModel;
        this.focal_length = this.inModel.params.focal_length;
        this.distortion = distortion;
    }

    public Point2D[] perspectiveToFishey(Point2D[] pp, double bound) {
        Point2D[] ans = new Point2D[pp.length];
        int index = 0;
        for (Point2D p : pp) {
            Point2D temp = new Point2D.Double(Double.NaN, Double.NaN);
            double xp = p.getX();
            double yp = p.getY();
            if ((Math.abs(xp) > 1e-6) || (Math.abs(yp) > 1e-6)) {
                double Rp = Math.sqrt(xp * xp + yp * yp);
                double Rf = this.distortion.mapping_function(Rp, bound);
                if (Rf >= 0) {
                    double xf = xp * Rf / Rp;
                    double yf = yp * Rf / Rp;
                    temp = new Point2D.Double(xf, yf);
                }
            } else {
                temp = new Point2D.Double(0, 0);
            }
            ans[index++] = temp;
        }
        return ans;
    }

    public Point2D[] fisheyToPerspective(Point2D[] pf) {
        Point2D[] ans = new Point2D[pf.length];
        int index = 0;
        for (Point2D p : pf) {
            Point2D temp = new Point2D.Double(Double.NaN, Double.NaN);
            double xf = p.getX();
            double yf = p.getY();
            if ((Math.abs(xf) > 1e-6) || (Math.abs(yf) > 1e-6)) {
                double Rf = Math.sqrt(xf * xf + yf * yf);
                double Rp = this.distortion.inverse_mapping_function(Rf);
                if (Rp >= 0) {
                    double xp = xf * Rp / Rf;
                    double yp = yf * Rp / Rf;
                    temp = new Point2D.Double(xp, yp);
                }
            } else {
                temp = new Point2D.Double(0, 0);
            }
            ans[index++] = temp;
        }
        return ans;
    }

    public Point2D computeOutputsize(Point2D[] pp) {
        double maximum_x = Double.NaN;
        double maximum_y = Double.NaN;
        double minimum_x = Double.NaN;
        double minimum_y = Double.NaN;
        boolean firsttime = true;
        for (Point2D p : pp) {
            if (firsttime) {
                firsttime = false;
                maximum_x = minimum_x = p.getX();
                maximum_y = minimum_y = p.getY();
            } else {
                if (maximum_x < p.getX()) {
                    maximum_x = p.getX();
                }

                if (minimum_x > p.getX()) {
                    minimum_x = p.getX();
                }

                if (maximum_y < p.getY()) {
                    maximum_y = p.getY();
                }

                if (minimum_y > p.getY()) {
                    minimum_y = p.getY();
                }
            }
        }
        System.out.println("maximum_x = " + maximum_x);
        System.out.println("minimum_x = " + minimum_x);
        System.out.println("maximum_y = " + maximum_y);
        System.out.println("minimum_y = " + minimum_y);

        return new Point2D.Double(maximum_x - minimum_x, maximum_y - minimum_y);
    }

    public static double clamp(double value) {
        if (value < 0) {
            value = 0;
        } else if (value > 1) {
            value = 1;
        }
        return value;
    }

    public void draw(WritableRaster destination, Raster source, Point2D destination_p, Point2D source_p) {

        int left_x = (int) Math.floor(source_p.getX());
        int right_x = (int) Math.ceil(source_p.getX());
        int up_y = (int) Math.floor(source_p.getY());
        int down_y = (int) Math.ceil(source_p.getY());
        int x = (int) source_p.getX();
        int y = (int) source_p.getY();
        int dx = (int) destination_p.getX();
        int dy = (int) destination_p.getY();

        if ((right_x >= source.getWidth()) || (left_x < 0)) {
            //System.out.println("bump x");
            return;
        }
        if ((down_y >= source.getHeight()) || (up_y < 0)) {
            //System.out.println("bump y");
            return;
        }
        for (int b = 0; b < 3; b++) {
            double Inw = source.getSample(left_x, up_y, b);
            double Ine = source.getSample(right_x, up_y, b);
            double Isw = source.getSample(left_x, down_y, b);
            double Ise = source.getSample(right_x, down_y, b);
            double weighted = 0;
            if (left_x == right_x) {
                if (up_y == down_y) {
                    weighted = Ise;
                } else {
                    weighted = Ise * (y - down_y) + Ine * (up_y - y);
                }
            } else if (up_y == down_y) {
                weighted = Isw * (right_x - x) + Ise * (x - left_x);
            } else {
                weighted += Inw * (right_x - x) * (down_y - y);
                weighted += Ine * (x - left_x) * (down_y - y);
                weighted += Isw * (right_x - x) * (y - up_y);
                weighted += Ise * (x - left_x) * (y - up_y);
            }
            destination.setSample(dx, dy, b, weighted);
        }
    }

    public static void weight(double value, WritableRaster output, WritableRaster mask, int x, int y, int b) {
        value += output.getSampleDouble(x, y, b);
        output.setSample(x, y, b, value);
        double vote = mask.getSampleDouble(x, y, b) + 1;
        mask.setSample(x, y, b, vote);
    }

    public CameraModel resample(Point resolution) {

        Point2D[] sensorpoints = this.inModel.toSensorCoords();
        Point2D[] tests = this.fisheyToPerspective(sensorpoints);
        Point2D output_sensor_size = this.computeOutputsize(tests);
        CameraUtils.CameraParams outparams = new CameraUtils.CameraParams(output_sensor_size, this.focal_length);
        int width = resolution.x;
        int height = resolution.y;
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);
        BufferedImage outImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        CameraModel outModel = new CameraModel(outImage, mask, outparams);
        Point2D[] imgcoords = outModel.fromSensorCoords(tests);

        List<Point2D> lista = new ArrayList<>();
        for (Point2D p : imgcoords) {
            Point xxx;
            xxx = new Point((int) Math.floor(p.getX()), (int) Math.floor(p.getY()));
            lista.add(xxx);
            xxx = new Point((int) Math.floor(p.getX()), (int) Math.ceil(p.getY()));
            lista.add(xxx);
            xxx = new Point((int) Math.ceil(p.getX()), (int) Math.floor(p.getY()));
            lista.add(xxx);
            xxx = new Point((int) Math.ceil(p.getX()), (int) Math.ceil(p.getY()));
            lista.add(xxx);
        }

        WritableRaster masked = mask.getRaster();
        for (Point2D a : lista) {
            masked.setSample((int) a.getX(), (int) a.getY(), 0, 1);
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int count = 0;
                count += masked.getSample((i + 1) % width, j, 0);
                count += masked.getSample((i - 1 + width) % width, j, 0);
                count += masked.getSample(i, (j + 1) % height, 0);
                count += masked.getSample(i, (j - 1 + height) % height, 0);
                if (count >= 3) {
                    masked.setSample(i, j, 0, 1);
                }
            }
        }

        lista.clear();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (masked.getSample(i, j, 0) == 1) {
                    lista.add(new Point2D.Double(i, j));
                    masked.setSample(i, j, 0, 255);
                }
            }
        }

        Point2D[] originals = lista.toArray(new Point2D[0]);
        Point2D[] backprojected = outModel.toSensorCoords(originals);
        int oldwidth = this.inModel.image.getWidth();
        int oldheight = this.inModel.image.getHeight();
        double bound = 0.5 * Math.sqrt(oldwidth * oldwidth + oldheight * oldheight);
        backprojected = this.perspectiveToFishey(backprojected, bound);
        backprojected = this.inModel.fromSensorCoords(backprojected);
        Raster source = this.inModel.image.getData();
        WritableRaster destination = outImage.getRaster();
        for (int i = 0; i < originals.length; i++) {
            this.draw(destination, source, originals[i], backprojected[i]);
        }
        return outModel;
    }
}
