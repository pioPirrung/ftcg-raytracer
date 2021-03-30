package de.hskl.imst.i.cgma.raytracer;
 
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import de.hskl.imst.i.cgma.raytracer.file.I_Sphere;
import de.hskl.imst.i.cgma.raytracer.file.RTFile;
import de.hskl.imst.i.cgma.raytracer.file.RTFileReader;
import de.hskl.imst.i.cgma.raytracer.file.RT_Object;
import de.hskl.imst.i.cgma.raytracer.file.T_Mesh;
import de.hskl.imst.i.cgma.raytracer.gui.IRayTracerImplementation;
import de.hskl.imst.i.cgma.raytracer.gui.RayTracerGui;

public class Raytracer00 implements IRayTracerImplementation {
    // viewing volume with infinite end
    private float fovyDegree;
    private float near;
    private float fovyRadians;

    RayTracerGui gui = new RayTracerGui(this);

    private int resx, resy; // viewport resolution
    private float h, w, aspect; // window height, width and aspect ratio

    Vector<RT_Object> objects;

    private Raytracer00() {
	try {

	    gui.addObject(RTFileReader.read(I_Sphere.class, new File("data/ikugel.dat")));

	    objects = gui.getObjects();

	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public void setViewParameters(float fovyDegree, float near) {
	    // set attributes fovyDegree, fovyRadians, near
    	this.fovyDegree = fovyDegree;
    	this.fovyRadians = (float) ((fovyDegree / 360) * 2 * Math.PI);
    	this.near = near;
    	
	    
	    // set attributes resx, resy, aspect
	    resx = gui.getResX();
	    resy = gui.getResY();
	    aspect = resx / resy;
	    
	    // set attributes h, w
	    h = (float) (2 * near * Math.tan(fovyDegree/2));
	    w = h * aspect;
	    //Workspace test
    }

    @Override
    public void doRayTrace() {
	float x, y, z;			// intersection point in viewing plane
	float rayEx, rayEy, rayEz;	// eye point==ray starting point
	float rayVx, rayVy, rayVz;	// ray vector
	Color color;

	// hardcoded viewing volume with fovy and near
	setViewParameters(90.0f, 1.0f);
	// set eye point
	rayEx = 0;
	rayEy = 0;
	rayEz = 0;
	
	z = -near;


	
	Random rd = new Random();
	// xp, yp: pixel coordinates
	for (int xp = 0; xp < resx; ++xp) {
	    for (int yp = 0; yp < resy; ++yp) {
		// for demo purposes
		// gui.setPixel(xp, yp, Color.WHITE.getRGB());	
		gui.setPixel(xp, yp, new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat()).getRGB());

		// x, y: view coordinates


		// ray vector

		
		
    		// get color or null along the ray
    		//color=traceRayAndGetColor...
    		// if color!=null set pixel with color
		
		
	    }
	}
    }

    // returns Color object or null if no intersection was found
    private Color traceRayAndGetColor(float rayEx, float rayEy, float rayEz, float rayVx, float rayVy, float rayVz) {
	// RTFile scene = gui.getFile();

	double minT = Float.MAX_VALUE;
	int minObjectsIndex = -1;

	RTFile scene;
	I_Sphere sphere;

	// loop over all scene objects to find the nearest intersection, that
	// is:
	// object with number minObjectIndex
	// minT is the minimal factor t of the ray equation s(t)=rayE+t*rayV
	// where the nearest intersection takes place
	for (int objectsNumber = 0; objectsNumber < objects.size(); objectsNumber++) {
	    scene = objects.get(objectsNumber);

	    // object is an implicit sphere?
	    if (scene instanceof I_Sphere) {
		sphere = (I_Sphere) scene;

		float t;
		
		// ray intersection uses quadratic equation
		float a, b, c, d;


		

		// positive discriminant determines intersection
		d = -42;
		// no intersection point? => next object
		if (d <= 0)
		    continue;

		// from here: intersection takes place!

		// calculate first intersection point with sphere along the
		// ray
		t = 0;

		// already a closer intersection point? => next object
		if (t >= minT)
		    continue;

		// from here: t < minT
		// I'm the winner until now!

		minT = t;
		minObjectsIndex = objectsNumber;

	    }
	}

	// no intersection point found => return with no result
	if (minObjectsIndex == -1)
	    return null;

	// intermediate version
	Random rd = new Random();
	return new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat());


    }

    public static void main(String[] args) {
	Raytracer00 rt = new Raytracer00();

	//rt.doRayTrace();
    }
}
