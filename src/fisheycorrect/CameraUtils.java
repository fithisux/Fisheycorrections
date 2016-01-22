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

/**
 *
 * @author orbit
 */
public class CameraUtils {

    //based on Altera's "A Flexible Architecture for Fisheye Correction in Automotive Rear-View Cameras"
    public static abstract class CameraDistortion {

        double focal_length;

        public CameraDistortion(double focal_length) {
            this.focal_length = focal_length;
        }

        public abstract double mapping_function(double Rp, double toler);

        public abstract double inverse_mapping_function(double Rf);
    }

    public static class EquidistantDistortion extends CameraDistortion {

        public EquidistantDistortion(double focal_length) {
            super(focal_length);
        }

        public double mapping_function(double Rp, double bound) {
            double theta = Math.atan(Rp / this.focal_length);
            double Rf = this.focal_length * theta;
            if (Rp > bound) {
                //System.out.println("too much");
                Rf = -Rf;
            }
            return Rf;
        }

        public double inverse_mapping_function(double Rf) {
            double theta = Rf / this.focal_length;
            double Rp = this.focal_length * Math.tan(theta);
            if (2 * theta > Math.PI) {
                //System.out.println("too much");
                Rp = -Rp;
            }
            return Rp;
        }
    }

    public static class EquisolidDistortion extends CameraDistortion {


        public EquisolidDistortion(double focal_length) {
            super(focal_length);
        }

        public double mapping_function(double Rp, double toler) {
            double theta = Math.atan(Rp / this.focal_length);
            double Rf = 2 * this.focal_length * Math.sin((theta / 2));
            if (Rp > toler) {
                //System.out.println("too much");
                Rf = -Rf;
            }
            return Rf;
        }

        public double inverse_mapping_function(double Rf) {
            double theta = 2 * Math.asin(Rf / (2 * this.focal_length));
            double Rp = this.focal_length * Math.tan(theta);
            if (2 * theta > Math.PI) {
                //System.out.println("too much");
                Rp = -Rp;
            }
            return Rp;
        }
    }

    public static class GoProParams {

        static double focal_length = 3;
        //static double sensor_width = 6.72;
        static double sensor_width = 5.67;
        //static double sensor_width = 7.14;
        //static double sensor_width = 3.9;    
        static Point2D input_sensor_size = new Point2D.Double(sensor_width, 0.75 * sensor_width);

        public static CameraParams getDefaults() {
            return new CameraUtils.CameraParams(GoProParams.input_sensor_size, GoProParams.focal_length);
        }
    }

    public static class CameraParams {

        Point2D sensor_size;
        double focal_length;

        public CameraParams(Point2D sensor_size, double focal_length) {
            this.sensor_size = sensor_size;
            this.focal_length = focal_length;
        }

        public Point2D getSensor_size() {
            return sensor_size;
        }

        public double getFocal_length() {
            return focal_length;
        }
    }
}
