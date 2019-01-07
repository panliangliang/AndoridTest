package com.example.liapan.hello;

import android.app.Activity;

import android.graphics.Bitmap;
import android.view.ContextMenu;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    private Bitmap bmp;
    private Bitmap originalBitmap;
    private Bitmap bmpTmp;
    private int w;
    private int h;
    protected ImageView iw, redView, greenView;
    private TextView tv;
    private BitmapFactory.Options o;
    private SeekBar seekbar, seekbar2;
    private Button button, saveButton, resetButton;
    private PopupMenu popupmenu;
    private Uri imageUri;
    String imagePath;
    int hueMin, hueMax;

    int[][] averageMask = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};
    int[][] gaussianMask = {{1, 2, 3, 2, 1}, {2, 6, 8, 6, 2}, {3, 8, 10, 8, 3}, {2, 6, 8, 6, 2}, {1, 2, 3, 2, 1}};
    int[][] h1Sobel = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
    int[][] h2Sobel = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};
    int[][] laplace4cx = {{0, 1, 0}, {1, -4, 1}, {0, 1, 0}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        o = new BitmapFactory.Options();
        o.inMutable = true;
        o.inScaled = false;

        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.vegetable, o);
        originalBitmap = bmp.copy(bmp.getConfig(), true);
        h = bmp.getHeight();
        w = bmp.getWidth();
        iw = (ImageView) findViewById(R.id.image);
        iw.setImageBitmap(bmp);


        //toGray(bmp);
        //toGrayLDE(bmp);
        //colorLDE(bmp);
        //togray_diminue_dynamique(bmp);
        //colorize(bmp,1.f);
        red_rest();
        //averageFilter(bmp);
        //gaussianFilter(bmp);
        //sobel(bmp);
        //laplace(bmp);
        //toGrayHistEqual(bmp);
        //colorHistEqual(bmp);


        tv = (TextView) findViewById(R.id.Taille);
        tv.setText(bmp.getHeight() + "x" + bmp.getWidth());




        resetButton = (Button) findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmp = originalBitmap.copy(originalBitmap.getConfig(), true);
                iw.setImageBitmap(bmp);
            }
        });


    }
    @Override

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu,v,menuInfo);
        MenuInflater inflater = getMenuInflater();
        menu.setHeaderTitle("Choose image");
        inflater.inflate(R.menu.context_menu,menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        super.onContextItemSelected(item);
        switch (item.getItemId()){
            case R.id.vegetable:
                bmp = BitmapFactory.decodeResource(getResources(),R.drawable.vegetable, o);
                break;
            case R.id.sky:
                bmp = BitmapFactory.decodeResource(getResources(),R.drawable.sky, o);
                break;

            default:
                return false;
        }
        originalBitmap = bmp.copy(bmp.getConfig(), true);
        Toast.makeText(getApplicationContext(), "You have chosen the image '" + item.getTitle() + "'", Toast.LENGTH_SHORT).show();
        h = bmp.getHeight();
        w = bmp.getWidth();
        tv.setText(bmp.getHeight() + "x" + bmp.getWidth());
        iw.setImageBitmap(bmp);
        return true;
    }

    //Menus definitions
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.toGray:
                toGray(bmp);
                break;
            case R.id.toGrayLDD:
                togray_diminue_dynamique(bmp);
                break;
            case R.id.colorLDE:
                colorLDE(bmp);
                break;
            case R.id.toGrayHistEqual:
                toGrayHistEqual(bmp);
                break;
            case R.id.colorHistEqual:
                colorHistEqual(bmp);
                break;

            case R.id.averageFilter:
                averageFilter(bmp);
                break;

            case R.id.sobel:
                sobel(bmp);
                break;
            case R.id.laplace:
                laplace(bmp);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        Toast.makeText(getApplicationContext(), "You have clicked on " + item.getTitle(), Toast.LENGTH_SHORT).show();
        return true;
    }


        /**
     * Converts an RGB bitmap to a gray one
     * @param bmp :
     *            the Bitmap that is converted
     */
        public void toGray (Bitmap bmp){

            int[] pixels = new int[h * w];

            bmp.getPixels(pixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < h * w; i++) {
                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);
                int lum = (30 * r + 59 * g + 11 * b) / 100;
                pixels[i] = Color.rgb(lum, lum, lum);
            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * Converts an RGB Bitmap to a gray one and extends the linear dynamic to improve the contrast
         * @param bmp :
         *            the Bitmap which is modified
         */
        public void toGrayLDE (Bitmap bmp){
            int min = 255;
            int max = 0;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int pixel = bmp.getPixel(x, y);
                    int r = Color.red(pixel);
                    int g = Color.green(pixel);
                    int b = Color.blue(pixel);
                    int lum = (30 * r + 59 * g + 11 * b) / 100;         //converts to a gray pixel
                    if (lum < min) {                                      //looking for the minimal value of gray levels
                        min = lum;
                    }
                    if (lum > max) {                                      //looking for the maximal value of gray levels
                        max = lum;
                    }
                    bmp.setPixel(x, y, Color.rgb(lum, lum, lum));
                }
            }
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    int pixel = bmp.getPixel(x, y);
                    int lum = Color.red(pixel);
                    int lde = (255 * (lum - min)) / (max - min);     //linear dynamic extension
                    bmp.setPixel(x, y, Color.rgb(lde, lde, lde));
                }
            }
        }
        /**
         * Improves the contrast of an RGB Bitmap extending its linear dynamic
         * @param bmp :
         *            the Bitmap which is modified
         */
        public void colorLDE (Bitmap bmp){

            int minR = 255, minG = 255, minB = 255;
            int maxR = 0, maxG = 0, maxB = 0;

            int[] pixels = new int[h * w];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < w * h; i++) {

                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);

                if (r < minR) {                                   //looking for the minimal value of red values
                    minR = r;
                }

                if (r > maxR) {                                   //looking for the maximal value of red values
                    maxR = r;
                }

                if (g < minG) {                                   //looking for the minimal value of green values
                    minG = g;
                }

                if (g > maxG) {                                   //looking for the maximal value of green values
                    maxG = g;
                }

                if (b < minB) {                                   //looking for the minimal value of blue values
                    minB = b;
                }

                if (b > maxB) {                                   //looking for the maximal value of blue values
                    maxB = b;
                }

            }

            for (int i = 0; i < w * h; i++) {

                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);

                int ldeR = (255 * (r - minR)) / (maxR - minR);     //linear dynamic extension increase for red
                int ldeG = (255 * (g - minG)) / (maxG - minG);     //linear dynamic extension increase for green
                int ldeB = (255 * (b - minB)) / (maxB - minB);     //linear dynamic extension increase for red

                pixels[i] = Color.rgb(ldeR, ldeG, ldeB);
            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * decrease the contrast of an RGB Bitmap
         * @param bmp :
         *            the Bitmap which to be modified
         */
        public void togray_diminue_dynamique (Bitmap bmp){

            int setoff = 30;
            int size = h * w;
            int[] pixels = new int[size];
            int[] gray = new int[size];
            int[] hist = new int[256];

            for (int i = 0; i < 256; i++) {
                hist[i] = 0;
            }

            int[] lut = new int[256];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);

            for (int i = 0; i < size; i++) {
                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);
                int gr = (30 * r + 59 * g + 11 * b) / 100;
                gray[i] = gr;
                hist[gr]++;
            }

            int min = 0;
            while (hist[min] == 0) {
                min++;
            }

            int max = 255;
            while (hist[max] == 0) {
                max--;
            }

            int min_dynamique = min + setoff;
            int max_dynamique = max - setoff;
            for (int i = 0; i < 256; i++) {
                lut[i] = (max_dynamique - min_dynamique) * (i - min) / (max - min) + min_dynamique;
            }

            for (int i = 0; i < size; i++) {
                gray[i] = lut[gray[i]];
                pixels[i] = Color.rgb(gray[i], gray[i], gray[i]);
            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
     * Builds a histogram
     * @param canal :
     *              the canal with which the histogram will be built
     * @return the histogram associated with the canal
     */
        public int[] getHistogram(int[] canal){

        int[] hist = new int[256];

        for(int i=0 ; i<h*w ; i++){                                             //Building the canal's histogram of a Bitmap bmp
            hist[canal[i]]++;
        }

        return hist;
    }
        /**
     * Calculates the cumulative histogram
     * @param hist :
     *             the histogram we want the cumulative histogram from
     * @return the cumulated histogram
     */
        public int[] getCumulativeHistogram(int[] hist){

        int c = 0;
        int[] cumulHist  = new int[256];

        for(int i = 0 ; i<256 ; i++){                                           //Building the cumulative histogram
            c = c + hist[i];
            cumulHist[i] = c;
        }

        return cumulHist;
    }
        /**
     * converts an RGB Bitmap to a gray one and equalises its histogram to improve the contrast.
     * @param bmp :
     *            the Bitmap which is modified
     */
        public void toGrayHistEqual(Bitmap bmp){

        int[] pixels = new int[h*w];
        bmp.getPixels(pixels,0,w,0,0,w,h);

        int[] grayCanal = new int[h*w];

        for(int i=0 ; i<h*w ; i++){                                             //Building the gray histogram of the Bitmap bmp

            int r = Color.red(pixels[i]);
            int g = Color.green(pixels[i]);
            int b = Color.blue(pixels[i]);

            grayCanal[i] = (30 * r + 59 * g + 11 * b) / 100;

        }

        int[] grayHist = getHistogram(grayCanal);
        int[] cumulHist  = getCumulativeHistogram(grayHist);

        for(int i=0 ; i<h*w ; i++){

            int R = Color.red(pixels[i]);
            double newlum = (cumulHist[R]*255)/(h*w);                           //histogram equalisation
            pixels[i] = Color.rgb((int) newlum, (int) newlum, (int) newlum);

        }

        bmp.setPixels(pixels,0,w,0,0,w,h);
    }
        /**
     * Improve the contrast of an RGB Bitmap equalising its histogram.
     * @param bmp :
     *            the Bitmap which will be modified
     */
        public void colorHistEqual (Bitmap bmp) {

        int[] pixels = new int[h * w];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        int[] grayCanal = new int[h*w];

        for (int i = 0; i < h * w; i++) {                                       //Building histograms for red, green and blue canals

            int r = Color.red(pixels[i]);
            int g = Color.green(pixels[i]);
            int b = Color.blue(pixels[i]);

            grayCanal[i] = (30 * r + 59 * g + 11 * b) / 100;

        }

        int[] grayHist = getHistogram(grayCanal);
        int[] cumulHist = getCumulativeHistogram(grayHist);

        for (int i = 0; i < h * w; i++) {

            int r = Color.red(pixels[i]);
            int g = Color.green(pixels[i]);
            int b = Color.blue(pixels[i]);

            double newR = (cumulHist[r] * 255) / (h * w);                  //Histogram equalisation for the red canal
            double newG = (cumulHist[g] * 255) / (h * w);                //Histogram equalisation for the green canal
            double newB = (cumulHist[b] * 255) / (h * w);                 //Histogram equalisation for the blue canal

            pixels[i] = Color.rgb((int) newR, (int) newG, (int) newB);
        }

        bmp.setPixels(pixels, 0, w, 0, 0, w, h);
    }

        public void colorize (Bitmap bmp,float tint){

            int[] pixels = new int[h * w];
            bmpTmp.getPixels(pixels, 0, w, 0, 0, w, h);

            float[] hsv = new float[3];

            for (int i = 0; i < h * w; i++) {

                Color.colorToHSV(pixels[i], hsv);
                hsv[0] = tint;
                pixels[i] = Color.HSVToColor(hsv);

            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         *Converts an RGB Bitmap to a gray one keeping only the red colour of pixels
         */
        public void red_rest () {
            int size = w * h;
            int[] pixels = new int[size];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < size; i++) {
                float[] hsv = new float[3];
                Color.colorToHSV(pixels[i], hsv);
                if ((hsv[0] > 15 && hsv[0] < 350)) {
                    int r = Color.red(pixels[i]);
                    int g = Color.green(pixels[i]);
                    int b = Color.green(pixels[i]);
                    int gray = (30 * r + 59 * g + 11 * b) / 100;
                    pixels[i] = Color.rgb(gray, gray, gray);
                }
            }
            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * Calculates the convolution of a canal of a Bitmap using a mask.
         * @param canal :
         *              a canal of colour of a Bitmap
         * @param mask :
         *             a mask which will ba applied to the canal to calculate the convolution of this canal
         * @return the canal on which the mask has been applied = the convolution of the canal.
         */
        public int[] convolution ( int[] canal, int[][] mask){

            int n = mask.length / 2;
            int[] canalConvo = new int[h * w];

            for (int i = n * (w + 1); i < h * w - n * (w + 1); i++) {                                //do not apply on side edges up and down

                int y = i / w;

                if (y % w != 0 && y % (w - 1) != 0) {                                  //do not apply on upper and lower edges
                    int convo = 0;

                    for (int u = -n; u <= n; u++) {

                        for (int v = -n; v <= n; v++) {

                            int K = mask[u + n][v + n];
                            int I = canal[i + u * w + v];
                            convo += I * K;

                        }
                    }

                    canalConvo[i] = convo;

                }
            }

            return canalConvo;
        }
        /**
         * Blurs a Bitmap changing a pixel by the average of its neighbour pixels.
         * Smooths the Bitmap and reduces the noise and the details in the Bitmap.
         * @param bmp :
         *            the Bitmap which is modified
         */
        public void averageFilter (Bitmap bmp){
            int[] pixels = new int[w * h];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            int[][] usedMask = averageMask;
            int n = usedMask.length / 2;
            int maskTotal = 0;
            for (int u = -n; u <= n; u++) {
                for (int v = -n; v <= n; v++) {
                    maskTotal += usedMask[u + n][v + n];                        //The sum of the mask's values
                }
            }
            int[] redCanal = new int[h * w];
            int[] greenCanal = new int[h * w];
            int[] blueCanal = new int[h * w];
            for (int i = 0; i < h * w; i++) {
                redCanal[i] = Color.red(pixels[i]);
                greenCanal[i] = Color.green(pixels[i]);
                blueCanal[i] = Color.blue(pixels[i]);
            }
            redCanal = convolution(redCanal, usedMask);
            greenCanal = convolution(greenCanal, usedMask);
            blueCanal = convolution(blueCanal, usedMask);
            for (int i = 0; i < h * w; i++) {
                pixels[i] = Color.rgb(redCanal[i] / maskTotal, greenCanal[i] / maskTotal, blueCanal[i] / maskTotal);
            }
            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * Blurs a Bitmap changing using a Gaussian Mask.
         * Smooths the Bitmap and reduces the noise and the details in the Bitmap (better than mediumFilter).
         * @param bmp :
         *            the Bitmap which is modified
         */
        public void gaussianFilter (Bitmap bmp){

            int[] pixels = new int[h * w];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            int[][] usedMask = gaussianMask;
            int n = usedMask.length / 2;
            int maskTotal = 0;

            for (int u = -n; u <= n; u++) {

                for (int v = -n; v <= n; v++) {

                    maskTotal += usedMask[u + n][v + n];                        //The sum of the mask's values

                }
            }

            int[] redCanal = new int[h * w];
            int[] greenCanal = new int[h * w];
            int[] blueCanal = new int[h * w];

            for (int i = 0; i < h * w; i++) {

                redCanal[i] = Color.red(pixels[i]);
                greenCanal[i] = Color.green(pixels[i]);
                blueCanal[i] = Color.blue(pixels[i]);

            }

            redCanal = convolution(redCanal, usedMask);
            greenCanal = convolution(greenCanal, usedMask);
            blueCanal = convolution(blueCanal, usedMask);

            for (int i = 0; i < h * w; i++) {

                pixels[i] = Color.rgb(redCanal[i] / maskTotal, greenCanal[i] / maskTotal, blueCanal[i] / maskTotal);

            }

            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * Shows the Sobel edges of a Bitmap.
         * @param bmp :
         *            the Bitmap which is modified
         */
        public void sobel (Bitmap bmp){
            gaussianFilter(bmp);
            int[] pixels = new int[h * w];
            int[] gradient = new int[h * w];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            int[] grayCanal = new int[h * w];
            for (int i = 0; i < h * w; i++) {
                int r = Color.red(pixels[i]);
                int g = Color.green(pixels[i]);
                int b = Color.blue(pixels[i]);
                int lum = (int) (0.3 * r + 0.59 * g + 0.11 * b);
                grayCanal[i] = lum;                                         //grayCanal is either red, blue or green canal as they have the same value
            }
            int[] gx = convolution(grayCanal, h1Sobel);                     //horizontal gradient
            int[] gy = convolution(grayCanal, h2Sobel);                     //vertical gradient
            for (int i = 1; i < h * w; i++) {                                 //calculating the norm's gradient
                gradient[i] = (int) Math.sqrt(gx[i] * gx[i] + gy[i] * gy[i]);
                if (gradient[i] > 255) {
                    gradient[i] = 255;
                }
                pixels[i] = Color.rgb(gradient[i], gradient[i], gradient[i]);
            }
            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }
        /**
         * Shows the Laplacian edges of a Bitmap.
         * @param bmp ;
         *            the Bitmap which is modified
         */
        public void laplace (Bitmap bmp){
            gaussianFilter(bmp);
            toGray(bmp);
            int[] pixels = new int[h * w];
            bmp.getPixels(pixels, 0, w, 0, 0, w, h);
            int[] grayCanal = new int[h * w];
            for (int i = 0; i < h * w; i++) {
                int lum = Color.red(pixels[i]);
                grayCanal[i] = lum;
            }
            int[] laplaceEdges = convolution(grayCanal, laplace4cx);
            for (int i = 0; i < h * w; i++) {
                if (laplaceEdges[i] < 0) {
                    laplaceEdges[i] = 0;
                }
                if (laplaceEdges[i] > 255 || laplaceEdges[i] != 0) {
                    laplaceEdges[i] = 255;
                }
                laplaceEdges[i] = 255 - laplaceEdges[i];
                pixels[i] = Color.rgb(laplaceEdges[i], laplaceEdges[i], laplaceEdges[i]);
            }
            bmp.setPixels(pixels, 0, w, 0, 0, w, h);
        }

    }
