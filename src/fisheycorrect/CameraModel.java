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
import java.awt.image.BufferedImage;

/**
 *
 * @author orbit
 */
public class CameraModel {

    BufferedImage image;
    BufferedImage mask;
    CameraUtils.CameraParams params;

    public CameraModel(BufferedImage image, BufferedImage mask, CameraUtils.CameraParams params) {
        this.image = image;
        this.mask = mask;
        this.params = params;
    }

    public void scale(double lambda1, double lambda2) {
        Point2D oldsize = params.sensor_size;
        Point2D newsize = new Point2D.Double(oldsize.getX() * lambda1, oldsize.getY() * lambda2);
        params.sensor_size = newsize;
    }

    public BufferedImage cropCorrected() {
        if (mask == null) {
            return this.image;
        }

        int hole_width = this.image.getWidth() / 2 - 1;
        int hole_height = this.image.getHeight() / 2 - 1;
        int width = this.image.getWidth() - 1;
        int height = this.image.getHeight() - 1;

        int start_i = -1;
        int end_i = -1;

        boolean startme_i = true;
        for (int i = 0; i < width; i++) {
            int val = this.image.getRaster().getSample(i, hole_height, 0);
            if (val != 0) {
                if (startme_i) {
                    startme_i = false;
                    start_i = i;
                }
            } else if (!startme_i) {
                end_i = i;
                break;
            }
        }

        int start_j = -1;
        int end_j = -1;
        boolean startme_j = true;
        for (int j = 0; j < height; j++) {
            int val = this.image.getRaster().getSample(hole_width, j, 0);
            if (val != 0) {
                if (startme_j) {
                    startme_j = false;
                    start_j = j;
                }
            } else if (!startme_j) {
                end_j = j;
                break;
            }
        }

        return this.image.getSubimage(start_i, start_j, end_i - start_i, end_j - start_j);
    }

    public Point2D[] fromSensorCoords(Point2D[] pp) {
        int index = 0;
        int width = this.image.getWidth();
        int height = this.image.getHeight();
        Point2D[] ans = new Point2D[pp.length];
        for (Point2D p : pp) {
            double temp_x = p.getX() / this.params.getSensor_size().getX();
            double temp_y = p.getY() / this.params.getSensor_size().getY();
            temp_x = FisheyCorrection.clamp(temp_x + 0.5) * (width - 1);
            temp_y = FisheyCorrection.clamp(temp_y + 0.5) * (height - 1);
            ans[index++] = new Point2D.Double(temp_x, temp_y);
        }
        //Alteratrick.toCSV(ans, "c:/workspace/drawer.csv");
        return ans;
    }

    public Point2D[] toSensorCoords() {
        int index = 0;
        int width = this.image.getWidth() - 1;
        int height = this.image.getHeight() - 1;
        Point2D[] ans = new Point2D[width * height];
        for (int i = 0; i < width; i++) {
            double ii = (double) i;
            for (int j = 0; j < height; j++) {
                double jj = (double) j;
                ans[index++] = new Point2D.Double(
                        this.params.getSensor_size().getX() * ((ii / width) - 0.5),
                        this.params.getSensor_size().getY() * ((jj / height) - 0.5)
                );
            }
        }
        return ans;
    }

    public Point2D[] toSensorCoords(Point2D[] pp) {
        int index = 0;
        int width = this.image.getWidth() - 1;
        int height = this.image.getHeight() - 1;
        Point2D[] ans = new Point2D[pp.length];
        for (Point2D p : pp) {
            ans[index++] = new Point2D.Double(
                    this.params.getSensor_size().getX() * ((p.getX() / width) - 0.5),
                    this.params.getSensor_size().getY() * ((p.getY() / height) - 0.5)
            );
        }
        return ans;
    }

}
