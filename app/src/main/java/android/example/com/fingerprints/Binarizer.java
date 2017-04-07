package android.example.com.fingerprints;

/**
 * Created by pranayponnappa on 12/24/16.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import android.gesture.Gesture;
import android.gesture.GestureStroke;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;



import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import Catalano.Core.IntPoint;
import Catalano.Imaging.Concurrent.Filters.BradleyLocalThreshold;
import Catalano.Imaging.Filters.ImageNormalization;
import Catalano.Imaging.Concurrent.Filters.RosinThreshold;
import Catalano.Imaging.Concurrent.Filters.SobelEdgeDetector;
import Catalano.Imaging.Corners.SusanCornersDetector;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Artistic.GradientMap;
import Catalano.Imaging.Filters.BrightnessCorrection;
import Catalano.Imaging.Filters.ConservativeSmoothing;
import Catalano.Imaging.Filters.ContrastCorrection;
import Catalano.Imaging.Filters.FourierTransform;
import Catalano.Imaging.Filters.FrequencyFilter;
import Catalano.Imaging.Filters.GammaCorrection;
import Catalano.Imaging.Filters.GaussianBlur;
import Catalano.Imaging.Filters.GaussianNoise;
import Catalano.Imaging.Filters.HistogramEqualization;
import Catalano.Imaging.Filters.HistogramStretch;
import Catalano.Imaging.Filters.HorizontalRunLengthSmoothing;
import Catalano.Imaging.Filters.Invert;
import Catalano.Imaging.Filters.Merge;
import Catalano.Imaging.Filters.Sharpen;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Filters.VerticalRunLengthSmoothing;
import Catalano.Imaging.Tools.GradientImage;
import Catalano.Math.ComplexNumber;
import Catalano.Math.Functions.Gamma;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_2D;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by kenny on 11/12/14.
 */
public class Binarizer extends Activity {
    //BitmapDrawable bd = (BitmapDrawable) view.getResources().getDrawable(R.drawable.backgrounds);
    int cellSize = 127;
    FastBitmap img;
    Gradient imggradient;
    Gradient[][] cellgradients;

    public Binarizer() {
        //Log.d("Binarizer", "Got to step 1") ;
        //Log.d("Binarizer", "Got to step 2") ;
        //System.out.println(openImage());
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inScaled = false;
        Bitmap bm = BitmapFactory.decodeFile(openImage(), opts);
        //Log.d("Binarizer", "Got to step 3");

        img = new FastBitmap(bm);
        bm.recycle();

        Log.d("openImage", "get here in openImage");
    }

    public void setCellsize(int size){
        cellSize = size;
    }

    public void testThings() {
        int cellsize2 = 25;//must be odd num - needs middle point

        BrightnessCorrection brightnessCorrection = new BrightnessCorrection(50);
        brightnessCorrection.applyInPlace(img);
        GammaCorrection gammaCorrection = new GammaCorrection(50);
        gammaCorrection.applyInPlace(img);
        Sharpen sharpen = new Sharpen();
        sharpen.applyInPlace(img);
        img.toGrayscale();
        GaussianBlur gaussianBlur = new GaussianBlur(getVarianceIteratively(getMeanOfFb(img), img));
        gaussianBlur.applyInPlace(img);
        int x = img.getGray(cellSize - 1, 0);
        cellgradients = new Gradient[img.getHeight() / cellsize2][img.getWidth() / cellsize2];
        imggradient = new Gradient(img);
        FastBitmap temp = new FastBitmap(img);
        Bitmap temp3 = temp.toBitmap();
        createBorderMask(img,cellsize2);
        x = img.getGray(cellSize - 1, 0);
        Bitmap temp4 = img.toBitmap();
        divideAndConquer4(0, 0, cellsize2, img);
        x = img.getGray(cellSize - 1, 0);
        cellgradients = cellgradients;
        Threshold threshold = new Threshold(cellSize);
        threshold.applyInPlace(img);
        temp4 = img.toBitmap();
        //divideAndConquer3(0,0,cellsize2,img);
        //temp = img.toBitmap();
        //Invert invert = new Invert();
        //invert.applyInPlace(img);
        //divideAndConquer3(0,0,cellsize2,img);
        //double oldM = getMeanOfFb(img);
/*
        //HistogramEqualization histogramEqualization = new HistogramEqualization();
        //histogramEqualization.applyInPlace(img);
        FastBitmap core = img;
        core.toGrayscale();



        ImageNormalization imageNormalization = new ImageNormalization(127, 127);
        imageNormalization.applyInPlace(img);

        //normalize(img, 4, 127);
        Bitmap temp = img.toBitmap();

        //int imgSize = (img.getHeight() < img.getWidth()) ? img.getHeight() : img.getWidth() ;
        //Log.d("testThings", "Height = " + img.getHeight() + "Width = " + img.getWidth() + "Size = " + imgSize);
        //img = split( img, 0, 0, 32, imgSize ) ;
        FourierTransform fourierTransform = new FourierTransform(img);
        fourierTransform.Forward();
        temp = fourierTransform.toFastBitmap().toBitmap();
        int radius = estimateRadius(fourierTransform.toFastBitmap() );


        Invert invert = new Invert();
        //invert.applyInPlace(img);
        fourierTransform = new FourierTransform(img);
        fourierTransform.Forward();

        FrequencyFilter frequencyFilter = new FrequencyFilter(0, radius*4);
        //frequencyFilter.ApplyInPlace(fourierTransform);
        //estimate radius here
        fourierTransform.Backward();
        img = fourierTransform.toFastBitmap();
        //invert.applyInPlace(img);
        cellgradients = new Gradient[img.getHeight() / cellsize2][img.getWidth() / cellsize2];
        imggradient = new Gradient(img);
        divideAndConquer2(0, 0, cellsize2, img);
        imageNormalization.applyInPlace(img);
        temp = img.toBitmap();
        Threshold threshold = new Threshold(cellSize);
        threshold.applyInPlace(img);
/*
        Threshold threshold;
        Merge merge;
        FastBitmap tempfb, newfb = new FastBitmap(img.getWidth(), img.getHeight());
        for(int i = 0; i < newfb.getWidth(); i++)
            for (int j = 0; j < newfb.getHeight(); j++)
                newfb.setGray(i,j,0);
        for (int i = 0; getMeanOfFb(newfb) < 140; i++) {
            tempfb = img;
            threshold = new Threshold(i);
            threshold.applyInPlace(tempfb);
            merge = new Merge(tempfb);
            merge.applyInPlace(newfb);
            temp = newfb.toBitmap();
        }
            //127 *8/7 works good, something to do w/ black space round edges
        img = newfb;*//*
        Merge merge = new Merge(core);


        invert.applyInPlace(img);
        //divideAndConquer3(0,0, cellsize2,img);
        //divideAndConquer3(0,0, cellsize2,img);
        //invert.applyInPlace(img);
        //merge.applyInPlace(img);
        //invert.applyInPlace(img);
        threshold.applyInPlace(img);
        */
        /*FastBitmap cell ;
        int csize = 25 ;

        for ( int i = 0; i < (img.getWidth() / csize) -1 ; i++) {
            Log.d("testThings", "i = " + i);
            for (int j = 0; j < (img.getHeight() / csize) -1 ; j++) {
                FastBitmap temp = new FastBitmap(csize, csize);
                Log.d("testThings", "j = " + j);
                for (int k = 0; k < csize; k++) {
                   Log.d("testThings", "k = " + k);
                    for (int l = 0; l < csize; l++) {
                        temp.setGray(k, l, img.getGray(i * csize + k, j * csize + l));
                        Log.d("testThings", "i = " + i + ", j = " + j + ", k = " + k + ", l = " + l + ",  at pixel " + i * csize + "x" + j * csize);
                    }
                }

                Bitmap testa = temp.toBitmap() ;
                temp.toGrayscale();
                FourierTransform ft = new FourierTransform(temp);
                ft.Forward();
                Bitmap testb = ft.toFastBitmap().toBitmap() ;
                FrequencyFilter ff = new FrequencyFilter(0, 20);
                ff.ApplyInPlace(ft);

                FastBitmap test = ft.toFastBitmap() ;
                testb = test.toBitmap() ;

                ft.Backward();
                temp = ft.toFastBitmap();

                for (int k = 0; k < csize; k++) {
                    for (int l = 0; l < csize; l++) {
                        img.setGray(i * csize + k, j * csize + l, temp.getGray( k, l));                    }
                }
            }
        }*/


    }

    public int estimateRadius(FastBitmap src){
        if (!src.isGrayscale()){
            return -1;
        }
        int x1 = src.getWidth()/2;
        int x2 = src.getWidth() - x1;
        int y1 = src.getHeight()/2;
        int y2 = src.getHeight() - y1;
        int r = 2;
        int intensity = 255;
        for(;intensity > 30 && r < x1 && r < y1;r++){
            intensity = (src.getGray(x1 - r, y1) + src.getGray(x1, y1-r) +src.getGray(x2+r, y2)+ src.getGray(x2, y2+r) )/4;
        }
        if(r%2 == 1)
            return r;
        return r+1;
    }



    public static Bitmap RotateBitmap(Bitmap source, float angle)
    //PROBLEMS
    //gray space around bitmap needs to be black,
    //bitmap is being shrunk because it is being fit into the same cell size but at an angle
    //also, cannot go back from rotation.
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public FastBitmap copyCell( FastBitmap from, FastBitmap to, int x, int y) {
        // Throw Exception if Width != Height for either cell to or from
        int nTo = to.getWidth() ;
        int nFrom = from.getWidth() ;
        int n = (nTo < nFrom) ? nTo : nFrom ;
        //Log.d("copy", "cell: " + n + ", " + to.getHeight() + " is " + ((n == nTo) ? "To" : "From")) ;

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if ( n == nTo ) to.setGray(i, j, from.getGray(x + i, y + j)) ;
                else to.setGray(x + i, y + j, from.getGray(i, j)) ;//y was x here, proofread dummy
            }
        }

        return to;
    }

    private FastBitmap Fourrier1dVert(FastBitmap src,int x, int min, int max){//works fine
        int n = src.getHeight(), y;
        if(n!=src.getWidth() || n <= x)
            return src;
        double o[] = new double[src.getHeight()];
        for(y = 0; y < n; y++){
            o[y] = src.getGray(x,y);
        }
        DoubleFFT_1D doubleFFT_1D = new DoubleFFT_1D(n);
        doubleFFT_1D.realForward(o);
        //Filtering


        doubleFFT_1D.realInverse(o, false);
        for(y = 0; y < n; y++){
            src.setGray(x,y, (int)o[y]);
        }
        return Fourrier1dVert(src, x+1, min, max);
    }

    public FastBitmap divideAndConquer3(int x, int y, int cellsize, FastBitmap src){
        if(x + cellsize <= src.getWidth() && y + cellsize <= src.getHeight()) {
            FastBitmap cell = new FastBitmap(cellsize, cellsize);

            cell.toGrayscale();
            //copypixels from image to cell
            Log.d("divide", "Origin: " + x + ", " + y);
            cell = copyCell(src, cell, x, y);
            int k;
            for (int i = 0; i < cellsize; i++)
                for (int j = 0; j < cellsize; j++)
                    if(cell.getGray(i,j) == 0){
                        for (k = 0; k < 3 ; k++)//                        for (k = 0; k <  ; k++)

                            for(int m = -10; m < 10; m++)
                                if((int)(i+k * Math.cos(cellgradients[x/cellsize][y/cellsize].avgdir +m/10)) >= 0 && (int)(j+k * Math.sin(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)) >= 0 && (int)(i+k * Math.cos(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)) < cellsize && (int)(j+k * Math.sin(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)) < cellsize)
                                    if (0== cell.getGray((int)(i+k * Math.cos(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)), (int)(j+k * Math.sin(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)))){
                                        for(int l = k; l > 0; l--)
                                            cell.setGray((int)(i+l * Math.cos(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)), (int)(j+l * Math.sin(cellgradients[x/cellsize][y/cellsize].avgdir+m/10)), 0);
                                    }
                    }

            src = copyCell(cell, src, x, y );
            cell.recycle();
            src = divideAndConquer3(x + cellsize, y, cellsize, src);
        }
        if(x + cellsize > src.getWidth() && y + cellsize < src.getHeight()) {
            return divideAndConquer3(0, y + cellsize, cellsize, src);
        }
        if(y + cellsize > src.getHeight())
            return src;
        else
            return src;
    }
    public FastBitmap divideAndConquer4(int x, int y, int cellsize, FastBitmap src){
        if(x + cellsize <= src.getWidth() && y + cellsize <= src.getHeight()) {
            FastBitmap cell = new FastBitmap(cellsize, cellsize);

            cell.toGrayscale();
            //copypixels from image to cell
            Log.d("divide", "Origin: " + x + ", " + y);
            cell = copyCell(src, cell, x, y);
            Bitmap temp = cell.toBitmap();
            cell.toGrayscale();
            Gradient gradient = new Gradient(cell);
            cellgradients[x/cellsize][y/cellsize] = gradient;
            //normalization is causing lines
            ImageNormalization imageNormalization = new ImageNormalization(127,127);
            imageNormalization.applyInPlace(cell);
            //normalize(cell,1,cellSize);
            //Threshold threshold = new Threshold(cellSize);
            //threshold.applyInPlace(cell);
            temp = cell.toBitmap();
            src=copyCell(cell,src,x,y);
            cell.recycle();
            src = divideAndConquer4(x + cellsize, y, cellsize, src);
        }
        if(x + cellsize > src.getWidth() && y + cellsize < src.getHeight()) {
            return divideAndConquer4(0, y + cellsize, cellsize, src);
        }
        if(y + cellsize > src.getHeight())
            return src;
        else
            return src;
    }
    public void createBorderMask(FastBitmap src, int cellsize) {

        src.toGrayscale();
        FastBitmap temp = new FastBitmap(src);
        ImageNormalization imageNormalization = new ImageNormalization(127, 127);
        imageNormalization.applyInPlace(temp);
        Threshold threshold = new Threshold(127);
        threshold.applyInPlace(temp);
        Bitmap temp1 = temp.toBitmap();
        int x0 = src.getWidth() / 2;
        int y0 = src.getHeight() / 2;
        int x1 = 0;
        int x2 = 0;
        int y1 = 0;
        int y2 = 0;
        int a = 0;
        int b = 0;
        for (int i = 0; i < temp.getWidth() / 2 && i <temp.getHeight(); i++){
            if (temp.getGray(x0 + i, y0) > 10)
                x1++;
            if (temp.getGray(x0 - i, y0) > 10)
                x2++;
            if (temp.getGray(x0, y0 + i) > 10)
                y1++;
            if (temp.getGray(x0, y0 - i) > 10)
                y2++;
        }
        a = (x1+x2)/2;
        b = (y1 +y2)/2;
        a-=cellsize;
        b-=cellsize;
        FastBitmap black = new FastBitmap(cellsize,cellsize);
        for(int i = 0; i < cellsize; i++)
            for (int j = 0; j < cellsize; j++)
                black.setGray(i,j,0);

        for(int i = 0; i <= src.getWidth() / 2; i+=cellsize)
            for (int j = 0; j <= src.getHeight() / 2; j+=cellsize) {
                temp1 = src.toBitmap();
                if (Math.pow(x0 - i, 2) / Math.pow(a, 2) + Math.pow((y0 - j), 2) / Math.pow(b, 2) >= 1) {
                    Log.d( "CenterTest", "center:" + x0 + " i: " + i + " j" + j ) ;

                    if (isValidCell(i, j, cellsize, src)) {

                        copyCell(black, src, i, j);
                    }
                    if (isValidCell(2 * x0 - i, 2 * y0 - j, cellsize, src)) {

                        copyCell(black, src, 2 * x0 - i, 2 * y0 - j);
                    }
                    if (isValidCell(i, 2 * y0 - j, cellsize, src)) {
                        copyCell(black, src, i, 2 * y0 - j);
                    }
                    if (isValidCell(2 * x0 - i, j, cellsize, src)) {
                        copyCell(black, src, 2 * x0 - i, j);
                    }
                }
            }
        //temp.recycle();
    }
    Boolean isValidCell(int x,int y,int cellsize,FastBitmap src ){
        return x >= 0 && x + cellsize <= src.getWidth() && y >= 0 && y + cellsize <= src.getHeight();
    }



    public FastBitmap divideAndConquer2(int x, int y, int cellsize, FastBitmap src){
        if(x + cellsize <= src.getWidth() && y + cellsize <= src.getHeight()) {
            FastBitmap cell = new FastBitmap(cellsize, cellsize);


            //copypixels from image to cell
            Log.d("divide", "Origin: " + x + ", " + y);
            cell = copyCell(src, cell, x, y);
            //Sharpen sharpen = new Sharpen();
            //sharpen.applyInPlace(cell);
            cell.toGrayscale();
            /*Gradient gradient = new Gradient(cell);
            cell = new FastBitmap(RotateBitmap(cell.toBitmap(), gradient.avgdir));
            Bitmap temp = cell.toBitmap();

            cell = Fourrier1dVert(cell, 0, 0, 100);
            cell = new FastBitmap(RotateBitmap(cell.toBitmap(), -gradient.avgdir));

            //copy back*/
            Gradient gradient = new Gradient(cell);
            cellgradients[x/cellsize][y/cellsize] = gradient;
            int s = cellsize / 4;
            int x1, x2, y1, y2;
            x1 = (int) ((cellsize / 2) + 1 + gradient.avgmag * Math.cos(gradient.avgdir));
            x2 = (int) ((cellsize / 2) + 1 - gradient.avgmag * Math.cos(gradient.avgdir));
            y1 = (int) ((cellsize / 2) + 1 + gradient.avgmag * Math.sin(gradient.avgdir));
            y2 = (int) ((cellsize / 2) + 1 - gradient.avgmag * Math.sin(gradient.avgdir));
            Bitmap temp = cell.toBitmap();
            //normalize(cell, 127,127);




            double[][] fft = new double[cellsize][cellsize * 2];
            for (int i = 0; i < cellsize; i++)
                for (int j = 0; j < cellsize; j++)
                    fft[i][j] = (double) cell.getGray(i, j);

            DoubleFFT_2D doubleFFT_2D = new DoubleFFT_2D(cellsize, cellsize);
            doubleFFT_2D.complexForward(fft);
            //center = 1+ cellsize/2, cellsize
            for (int i = 0; i < cellsize; i++)
                for (int j = 0; j < cellsize; j++)
                    if (!((x1 - s < i && i < x1 + s) && (y1 - s < j && j < y2 + s)) ||
                            ((x2 - s < i && i < x2 + s) && (y2 - s < j && j < y2 + s))) {
                        if (Math.hypot(fft[i][2 * j], fft[i][2 * j + 1]) < gradient.avgmag)
                            fft[i][2 * j] = 0;
                        fft[i][2 * j + 1] = 0;

                    }
                    else
                        fft[i][2*j] += gradient.avgmag;

            doubleFFT_2D.complexInverse(fft, false);
            for (int i = 0; i < cellsize; i++)
                for (int j = 0; j < cellsize; j++) {
                    if (fft[i][j] <= 255 && fft[i][j] >= 0)
                        cell.setGray(i, j, (int) fft[i][j]);
                }
            //to here*/





            //standard ff based on avgdir
            /*FrequencyFilter frequencyFilter;


            int k1 = Math.abs((int) (gradient.avgmag * Math.sin(gradient.avgdir)));
            int k2 = Math.abs((int) (gradient.avgmag * Math.cos(gradient.avgdir)));
            ComplexNumber[][] c = fourierTransform.getData();

            if (k1 == k2) {
                k1 = 0;
                k2 = (int)(2*gradient.avgmag);
            }
            else if (k1>k2) {
                int tempint = k2;
                k2 = k1;
                k1 = tempint;
            }
            Log.d("divide", "k1: " + k1 + ", k2: " + k2);
            for ( int i = 0; i < cellsize; i++ )
                for (int j = 0; j < cellsize; j++) {
                    if(((cellsize /2)+1  - s < i && i < (cellsize/2)+1 + s) && (cellsize /2) +1  - s < j && j < (cellsize/2) + 1 + s){
                    } else if(c[i][j].getMagnitude() > k2){
                        c[i][j].real = gradient.avgmag;
                        c[i][j].imaginary = gradient.avgmag;
                    }
                    else if(c[i][j].getMagnitude() < k1) {
                        c[i][j].real = 0;
                        c[i][j].imaginary = 0;
                    }

                }
            cell.toGrayscale();
            //For some reason, the frequency filters are not working
            */


            //Bitmap temp2 = fourierTransform.toFastBitmap().toBitmap();
            //frequencyFilter = new FrequencyFilter(0,45);
            //frequencyFilter.ApplyInPlace(fourierTransform);
            //fourierTransform.Backward();
            //cell = fourierTransform.toFastBitmap();
            //GaussianNoise gaussianNoise = new GaussianNoise(127);
            src = copyCell(cell, src, x, y );
            cell.recycle();
            src = divideAndConquer2(x + cellsize, y, cellsize, src);
        }
        if(x + cellsize > src.getWidth() && y + cellsize < src.getHeight()) {
            return divideAndConquer2(0, y + cellsize, cellsize, src);
        }
        if(y + cellsize > src.getHeight())
            return src;
        else
            return src;
    }


    public int getSumOfFastbitmap(FastBitmap fb, int x, int y, int sum){
        if(x < fb.getHeight() - 1){
            sum = sum + fb.getGray(x,y);
            return getSumOfFastbitmap(fb, x + 1, y, sum);
        }
        else {
            if (y < fb.getWidth()){
                return getSumOfFastbitmap(fb, 0, y + 1, sum);
            }
            else{
                return sum;
            }
        }

    }

    public Bitmap getBitmap() {

        return img.toBitmap();

    }

    public class Gradient {
        int x, y, hdiv, wdiv;
        double h, w;
        float avgdir;
        float avgmag;
        point[][] map;

        public class point {
            double magnitude;
            double direction;//in radians!!!!
            public point(double w, double h) {
                magnitude = Math.hypot(w,h);
                direction = Math.atan2(h,w);//range is -pi to pi, not 0 to 2pi!!!!


            }


        }
        public Gradient(FastBitmap src) {

            map = new point[src.getWidth()][src.getHeight()];
            h = 0;
            w = 0;
            hdiv = 0;
            wdiv = 0;


            for (x = 0; x < src.getWidth(); x++) {
                for (y = 0; y < src.getHeight(); y++) {

                    //Log.d("gradient", "x" + x+ " y"+ y);
                    if(x == 0 )
                        w = (src.getGray(x+1,y) - src.getGray(x,y))/2;
                    if(y == 0)
                        h = (src.getGray(x, y+1) - src.getGray(x,y))/2;
                    if(x == src.getWidth()-1)
                        w = (src.getGray(x,y) - src.getGray(x-1,y))/2;
                    if(y == src.getHeight()-1)
                        h = (src.getGray(x, y) - src.getGray(x, y - 1)) / 2;
                    if( 0< x && x < src.getWidth()-1)
                        w = (src.getGray(x+1, y) - src.getGray(x-1, y))/2;
                    if( 0< y && y < src.getHeight()-1)
                        h = (src.getGray(x, y+1) - src.getGray(x, y-1))/2;

                    if(0 <= x -1) {
                        if (0 <= y - 1) {
                            h+= ((2/Math.sqrt(2))*src.getGray(x,y)-src.getGray(x-1, y-1));
                            w+= ((2/Math.sqrt(2))*src.getGray(x,y)-src.getGray(x-1, y-1));
                            hdiv++;
                            wdiv++;
                        }
                        w+= (src.getGray(x,y)-src.getGray(x-1,y));
                        wdiv++;
                    }

                    if(0 <= y -1) {
                        if (x + 1 < src.getWidth()) {
                            h+= ((2/Math.sqrt(2))*src.getGray(x,y)-src.getGray(x+1, y-1));
                            w+= ((2/Math.sqrt(2))*src.getGray(x+1,y-1)-src.getGray(x, y));
                            hdiv++;
                            wdiv++;
                        }
                        h+= (src.getGray(x,y)-src.getGray(x,y-1));
                        hdiv++;
                    }
                    if(x+1< src.getWidth()) {
                        if (y+1 < src.getHeight()) {
                            h+= ((2/Math.sqrt(2))*src.getGray(x+1,y+1)-src.getGray(x, y));
                            w+= ((2/Math.sqrt(2))*src.getGray(x+1,y+1)-src.getGray(x, y));
                            hdiv++;
                            wdiv++;
                        }
                        w+= (src.getGray(x+1,y)-src.getGray(x,y));
                        wdiv++;
                    }
                    if(y+1 < src.getHeight()) {
                        if (0 <= x -1) {
                            h+= ((2/Math.sqrt(2))*src.getGray(x,y)-src.getGray(x-1, y+1));
                            w+= ((2/Math.sqrt(2))*src.getGray(x-1,y+1)-src.getGray(x, y));
                            hdiv++;
                            wdiv++;
                        }
                        h += (src.getGray(x,y+1)-src.getGray(x,y));
                        hdiv++;
                    }
                    map[x][y] = new point(w/wdiv, h/hdiv);
                    this.avgdir += map[x][y].direction;
                    this.avgmag += map[x][y].magnitude;
                }
            }




            this.avgdir /= src.getHeight()*src.getWidth();
            this.avgmag /= src.getHeight()*src.getWidth();
        }
    }



    // Different wa y of loading image
    private String openImage() {
        String path;
        //File img;
        File imgPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Fingerprints");
        Log.d("openImage", "get here in openImage");

        // open image (temporary way of opening image)
        path = imgPath.getAbsolutePath();

        //to make this test with fingerprint image included in package
        //createFingerPrintThere(path);
        path = path + File.separator + "Fingerprint.jpg";
        return path;
        //img = new File(path);
        //return img;
    }

    //Filter Tools
    //image should be atleast in grayscale (if not binary) before using
    private void normalize(FastBitmap img){
        double M = getMeanOfFb(img), V = getVarianceIteratively(M, img), wantedM = 75, wantedV = 5;
        int h = img.getHeight(), w = img.getWidth(), i, j;
        for(i = 0; i < h; i++){
            for(j = 0; j < w; j++){
                if(img.getGray(i,j) > M){
                    img.setGray(i,j, (int) (wantedM + Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
                else{
                    img.setGray(i,j, (int) (wantedM - Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
            }
        }
    }

    private void normalize(FastBitmap img, int wantedV){
        double M = getMeanOfFb(img), V = getVarianceIteratively(M, img), wantedM = 75;
        int h = img.getHeight(), w = img.getWidth(), i, j;
        for(i = 0; i < h; i++){
            for(j = 0; j < w; j++){
                if(img.getGray(i,j) > M){
                    img.setGray(i,j, (int) (wantedM + Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
                else{
                    img.setGray(i,j, (int) (wantedM - Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
            }
        }
    }

    private void normalize(FastBitmap img, int wantedV, int wantedM){
        double M = getMeanOfFb(img), V = getVarianceIteratively(M, img);
        int h = img.getHeight(), w = img.getWidth(), i, j;
        for(i = 0; i < h; i++){
            for(j = 0; j < w; j++){
                if(img.getGray(i,j) > M){
                    img.setGray(i,j, (int) (wantedM + Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
                else{
                    img.setGray(i,j, (int) (wantedM - Math.pow(wantedV * (img.getGray(i,j) - M)*(img.getGray(i,j) - M)/V, 2)));
                }
            }
        }
    }
    private double getMeanOfFb(FastBitmap fb){
        int h = fb.getHeight(), w = fb.getWidth(), i, j;
        double sum = 0;
        for(i = 0; i < h; i++){
            for(j = 0; j < w; j++){
                //Log.d("getMeanOfFb", "i = " + i+ ", j = "+ j + ", h = " +h+ ", w = " + w  );
                sum += fb.getGray(i,j);
            }
        }
        return sum / (h * w);
    }
    private double getVarianceIteratively(double M, FastBitmap fb){
        int h = fb.getHeight(), w = fb.getWidth(), i, j;
        double sum = 0;
        for(i = 0; i < h; i++){
            for(j = 0; j < w; j++){
                sum += ((fb.getGray(i,j)- M)*(fb.getGray(i,j)- M));
            }
        }
        return sum / (h * w);
    }

    public void cropDynamically() {
        Bitmap bm = img.toBitmap();
        int startx = bm.getWidth() / 3;
        int starty = bm.getHeight() / 3;
        int endx = bm.getWidth() - 2 * startx;
        int endy = bm.getHeight() - 2 * starty;
        System.out.println("" + bm.getWidth() + ", " + bm.getHeight() + ", " + startx + ", " + starty + ", " + endx + ", " + endy);
        Log.d("crop", "initialized vars");
        Bitmap croppedBm = Bitmap.createBitmap(bm, startx, starty, endx, endy);
        Log.d("crop", "crop applied");
        Bitmap scaledBm = Bitmap.createScaledBitmap(croppedBm, 500, 500, false);//was 1024 x 768
        Log.d("crop", "bitmap scaled");
        img = new FastBitmap(scaledBm);
        bm.recycle();
    }

    public class GestureUtil {
        private static final boolean BITMAP_RENDERING_ANTIALIAS = true;
        private static final boolean BITMAP_RENDERING_DITHER = true;
        private static final float BITMAP_RENDERING_WIDTH = 10;
        private static final int NUM_SAMPLES = 20;

        public static Bitmap toBitmap(Gesture gesture, int color, float strokeWidth) {
            RectF bounds = gesture.getBoundingBox();
            int width=(int)bounds.width();
            int height=(int)bounds.height();
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);

            int edge=0;
            //canvas.translate(edge, edge);

            final Paint paint = new Paint();
            paint.setAntiAlias(BITMAP_RENDERING_ANTIALIAS);
            paint.setDither(BITMAP_RENDERING_DITHER);
            paint.setColor(color);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(BITMAP_RENDERING_WIDTH);

            final ArrayList<GestureStroke> strokes = gesture.getStrokes();
            final int count = strokes.size();

            for (int i = 0; i < count; i++) {
                Path path = strokes.get(i).toPath(width - 2 * edge, height - 2 * edge, NUM_SAMPLES);
                canvas.drawPath(path, paint);
            }

            return bitmap;
        }
    }



}


    /*
    trying to include image in app so we can test on emus and what not easily
    private void createFingerPrintThere(String path){
        InputStream inputstream = null;
        try {
            inputstream = this.getResources().getAssets().open("test.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        File dest = new File(path);
        Bitmap bitmap = BitmapFactory.decodeStream(inputstream);
        try {
            FileOutputStream out;
            out = new FileOutputStream(dest);
            bitmap.compress(Bitmap.CompressFormat.JPEG , 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        /*
        OutputStream stream = null;
        try{
            stream = new FileOutputStream(path);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/

    /*private File openImage(){
        String path = getFilesDir() + File.separator + "res" + File.separator
        							+ "drawable-hdpi" + File.separator + "test.jpg";
        File img = new File(path);
        return img;
    }
}*/

    /*
    public FastBitmap copyCell (FastBitmap from, int x_or, int y_or, int cellSize, FastBitmap to, int x, int y) {

        if ( x < 0 || y < 0 || x_or < 0 || y_or < 0 ) return to ; // ERROR. Throw an exception?

        if ( cellSize > 1 ) {
            //Split into top left (current), top right, bottom left, and bottom right.
            //Each call passes the 'to' returned by the previous call. 'to' is modified 4 times.
            Log.d("cell", "Call 1: Size: " + cellSize + " Origin = (" + x_or + ", " + y_or + "). Offset by: (" + x + ", " + y + ")");
            to = copyCell( from, x_or, y_or, cellSize/2, to, x ,y ) ;
            Log.d("cell", "Call 2: Size: " + cellSize + " Origin = (" + x_or + ", " + y_or + "). Offset by: (" + x + ", " + y + ")");
            to = copyCell( from, x_or + cellSize/2, y_or, cellSize/2, to, x + cellSize/2 - 1,y ) ;
            Log.d("cell", "Call 3: Size: " + cellSize + " Origin = (" + x_or + ", " + y_or + "). Offset by: (" + x + ", " + y + ")");
            to = copyCell( from, x_or, y_or + cellSize/2, cellSize/2, to, x ,y + cellSize/2 - 1) ;
            Log.d("cell", "Call 4: Size: " + cellSize + " Origin = (" + x_or + ", " + y_or + "). Offset by: (" + x + ", " + y + ")");
            to = copyCell( from, x_or + cellSize/2, y_or + cellSize/2, cellSize/2, to, x + cellSize/2 - 1,y + cellSize/2 - 1 ) ;
        }
        else {
            to.setGray( x, y, from.getGray( x_or + x, y_or + y ));
        }

        return to ;

    }

    // Built for 512x512 image, cellsize of 32
    public FastBitmap split( FastBitmap src, int x, int y, int cellSize, int imgSize ) {

        if ( imgSize <= cellSize ) {
            // Create a small cell
            FastBitmap cell = new FastBitmap(cellSize,cellSize) ;
            Log.d("split", "Creating cell of size" + cellSize) ;
            // Copy corresponding pixels from image to cell
            cell = copyCell( src, x, y, imgSize, cell, 0, 0 ) ;
            // INSERT CODE THAT DOES USEFUL THINGS TO CELL HERE
            // Copy modified cell back into image
            Log.d("split", "Uncreating cell of size" + cellSize) ;
            src = copyCell( cell, 0, 0, imgSize, src, x, y ) ;
        }
        else {
            src = split( src, x, y, cellSize, imgSize/2 ) ;
            src = split( src, x + imgSize/2, y, cellSize, imgSize/2 ) ;
            src = split( src, x, y + imgSize/2, cellSize, imgSize/2 ) ;
            src = split( src, x + imgSize/2, y + imgSize/2, cellSize, imgSize/2 ) ;
        }

        return src ;

    }

    public FastBitmap copyPixels(FastBitmap src, int xstartsrc, int ystartsrc, int xendsrc, int yendsrc, int xpos, int ypos, FastBitmap dest, int xstartdest, int ystartdest, int xenddest, int yenddest, int cutoff) {
        //Log.d("copyPixels", "xpos" +xpos);
        if (((xstartdest + xpos < xenddest) && (xstartsrc + xpos < xendsrc)) && ((ystartdest + ypos < yenddest) && (ystartsrc + ypos < yendsrc))){//< for non divisable cell sizes
            //Log.d("copyPixels", "xstartsrc = " + xstartsrc + ", ystartsrc = " + ystartsrc + ", xendsrc = " + xendsrc + ", yendsrc = " + yendsrc + ", xpos = " + xpos + ", ypos = " + ypos + ", xstartdest = " + xstartdest + ", ystartdest = " + ystartdest + ", xenddest = " + xenddest + ", yenddest = " + yenddest);
                //adaptive thresholding, disabled when last param is less than 0
            if (cutoff >= 0) {
                if (src.getGray(xstartsrc + xpos, ystartsrc + ypos) <= cutoff){
                    dest.setGray(xstartdest + xpos, ystartdest + ypos, 0);
                    return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, xpos + 1, ypos, dest, xstartdest, ystartdest, xenddest, yenddest, cutoff);
                }
                else{
                    dest.setGray(xstartdest + xpos, ystartdest + ypos, 255);
                    return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, xpos + 1, ypos, dest, xstartdest, ystartdest, xenddest, yenddest, cutoff);
                }
            }


            else {
                dest.setRGB(xstartdest + xpos, ystartdest + ypos, src.getRGB(xstartsrc + xpos, ystartsrc + ypos));
                if((xstartdest + xpos == xenddest) && (xstartsrc + xpos == xendsrc))
                    return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, 0, ypos + 1, dest, xstartdest, ystartdest, xenddest, yenddest, cutoff);
                return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, xpos + 1, ypos, dest, xstartdest, ystartdest, xenddest, yenddest, cutoff);
            }
        }
        //moves down line
        else {
            if ((xstartdest + xpos > xenddest) && (xstartsrc + xpos > xendsrc)) {//>= for non divisable cell sizes
                return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, 0, ypos + 1, dest, xstartdest, ystartdest, xenddest, yenddest, cutoff);
            }
            else {
                return dest;
            }
        }
    }


    public void divideAndConquer(int x, int y, int cellsize) {
        Log.d("divideAndConquer", "x = " + x + ", y = " + y + ",w = " + img.getWidth() + ", h = " + img.getHeight());
        if (x + cellsize >= img.getHeight() && y >= img.getWidth()){}
        else {
            if ((x + cellsize) < img.getHeight() && y < img.getWidth()) {
                FastBitmap cell = new FastBitmap(cellsize , cellsize);
                cell = copyPixels(img, y, x, x + cellsize, y + cellsize, 0, 0, cell, 0, 0, cellsize, cellsize, -1);//switched x,y
                //Gradient gradient = new Gradient(cell);
                cell.toGrayscale();
                FourierTransform ft = new FourierTransform(cell);
                ft.Forward();
                FrequencyFilter ff = new FrequencyFilter(3,30);
                ff.ApplyInPlace(ft);

                FastBitmap test = ft.toFastBitmap() ;


                ft.Backward();
                cell = ft.toFastBitmap();
                img = copyPixels(cell, 0, 0, cellsize, cellsize, 0, 0, img, y, x, x + cellsize, y + cellsize, -1);//switched x,y

                HistogramEqualization histo = new HistogramEqualization();
                histo.applyInPlace(cell);
                cell.toGrayscale();
                int cutoff  = getSumOfFastbitmap(cell, 0, 0, 0) /(cellsize*cellsize);
                Log.d("divideAndConquer", "cuttoff = " + cutoff);

                if (cutoff >= 10)
                    cutoff -= 10;

                img = copyPixels(cell, 0, 0, cellsize, cellsize, 0, 0, img, x, y, x + cellsize, y + cellsize, cutoff);

cell.recycle();
        divideAndConquer(x + cellsize, y, cellsize);
        } else {
        if (y < img.getWidth()) {
        Log.d("divideAndConquer", "move down line");
        divideAndConquer(0, y + cellsize, cellsize);
        } else {
        Log.d("divideAndConquer", "Shouldn't be here");
        }
        }
        }
        }

    public FastBitmap copyPixels(FastBitmap src, int xstartsrc, int ystartsrc, int xendsrc, int yendsrc, int xpos, int ypos, FastBitmap dest, int xstartdest, int ystartdest, int xenddest, int yenddest) {
        Log.d("copyPixels", "xstartsrc " + xstartsrc+ " xendsrc " + xendsrc + " xpos " + xpos + " xstartsrc " + xstartdest+ " xstartdest" + xstartsrc+ " xenddest " + xenddest + " ystartsrc " + ystartsrc+ " yendsrc " + yendsrc + " ypos " + ypos + " ystartsrc " + ystartsrc+ " ystartdest " + ystartdest+ " yenddest " + yenddest);
        if (((xstartsrc + xpos < xendsrc) && (xstartdest + xpos < xenddest)) && ((ystartdest + ypos < yenddest) && (ystartsrc + ypos < yenddest))) {//should the ys be <=?

            Log.d("copyPixels", "color:" +src.getGray(xstartsrc + xpos, ystartsrc + ypos) );
            dest.setGray(xstartdest + xpos, ystartdest + ypos, src.getGray(xstartsrc + xpos, ystartsrc + ypos));
            return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, xpos + 1, ypos, dest, xstartdest, ystartdest, xenddest, yenddest);
        }
        else if (((xstartsrc + xpos == xendsrc) && (xstartdest + xpos == xenddest)) && ((ystartdest + ypos < yenddest) && (ystartsrc + ypos < yenddest))) {
            return copyPixels(src, xstartsrc, ystartsrc, xendsrc, yendsrc, 0, ypos + 1, dest, xstartdest, ystartdest, xenddest, yenddest);
        }
        else if (((ystartdest + ypos >= yenddest) && (ystartsrc + ypos >= yenddest))) {
            return dest;
        }
        else{
            Log.d("copyPixels", "You fucked something up.");
            return dest;
        }
    }


*/