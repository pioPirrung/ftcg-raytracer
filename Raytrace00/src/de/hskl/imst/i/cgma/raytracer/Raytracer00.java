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

	// one hardcoded point light as a minimal solution :-(
	private float[] Ia = { 0.25f, 0.25f, 0.25f }; // ambient light color
	private float[] Ids = { 1.0f, 1.0f, 1.0f }; // diffuse and specular light
	// color
	private float[] ICenter = { 4.0f, 4.0f, 2.0f }; // center of point light

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
		aspect = (float) resx / resy;

		// set attributes h, w
		h = (float) (2.0f * near * Math.tan(fovyRadians / 2.0f));
		w = h * aspect;
	}

	@Override
	public void doRayTrace() {
		float x, y, z; // intersection point in viewing plane
		float rayEx, rayEy, rayEz; // eye point==ray starting point
		float rayVx, rayVy, rayVz; // ray vector
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
				// gui.setPixel(xp, yp, new Color(rd.nextFloat(), rd.nextFloat(),
				// rd.nextFloat()).getRGB());

				// x, y: view coordinates
				x = ((float) xp / (float) (resx - 1)) * w - w / 2;
				y = (((float) (resy - 1) - (float) yp) / (float) (resy - 1)) * h - h / 2;

				// ray vector
				rayVx = x - rayEx;
				rayVy = y - rayEy;
				rayVz = z - rayEz;

				// get color or null along the ray
				// color=traceRayAndGetColor...
				// if color!=null set pixel with color
				color = traceRayAndGetColor(rayEx, rayEy, rayEz, rayVx, rayVy, rayVz);
				if (color != null) {
					gui.setPixel(xp, yp, color.getRGB());
				}

			}
		}
	}

	// returns Color object or null if no intersection was found
	private Color traceRayAndGetColor(float rayEx, float rayEy, float rayEz, float rayVx, float rayVy, float rayVz) {
		// RTFile scene = gui.getFile();

		double minT = Float.MAX_VALUE;
		int minObjectsIndex = -1;

		float[] minIP = new float[3];
		float[] minN = new float[3];
		float[] minMaterial = new float[3];
		float minMaterialN = 1;

		float[] v = new float[3];
		float[] l = new float[3];

		// TODO: HELLO
		// viewing vector at intersection point
		// v[0] =
		// v[1] =
		// v[2] =

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
				// a = rayV {scalar} rayV
				// b = 2 * rayV {scalar} eyeV - m (m mittelpunkt von kreis)
				// c = (eyeV - m) * (eyeV - m) - r^2
				float a, b, c, d;
				float mex, mey, mez;
				mex = rayEx - sphere.center[0];
				mey = rayEy - sphere.center[1];
				mez = rayEz - sphere.center[2];
				a = rayVx * rayVx + rayVy * rayVy + rayVz * rayVz;
				b = 2 * ((rayVx * mex) + (rayVy * mey) + (rayVz * mez));
				c = mex * mex + mey * mey + mez * mez - (float) Math.pow(sphere.radius, 2);

				// positive discriminant determines intersection
				d = (float) Math.pow(b, 2) - 4 * a * c;

				// no intersection point? => next object
				if (d <= 0)
					continue;

				// from here: intersection takes place!

				// calculate first intersection point with sphere along the
				// ray
				t = (float) ((-b - Math.sqrt(d)) / (2 * a));

				// already a closer intersection point? => next object
				if (t >= minT)
					continue;

				// from here: t < minT
				// I'm the winner until now!

				minT = t;
				minObjectsIndex = objectsNumber;

				// TODO: HELLO 2
				// prepare everything for phong shading
				// the intersection point
				// minIP[0] =
				// minIP[1] =
				// minIP[2] =

				// the normal vector at the intersection point
				// minN[0] =
				// minN[1] =
				// minN[2] =

				// the material
				minMaterial = sphere.material;
				minMaterialN = sphere.materialN;

			}
		}

		// no intersection point found => return with no result
		if (minObjectsIndex == -1)
			return null;

		//TODO: HELLO 3
		// light vector at the intersection point
		// l[0] =
		// l[1] =
		// l[2] =

		// decide which shading model will be applied
		// implicit: only phong shading available => shade=illuminate
		if (objects.get(minObjectsIndex) instanceof I_Sphere)
			return phongIlluminate(minMaterial, minMaterialN, l, minN, v, Ia, Ids);

		return null;

		// intermediate version
		// Random rd = new Random();
		// return new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat());

	}

	//TODO: PHONG
	// calculate phong illumination model with material parameters material and
	// materialN, light vector l, normal vector n, viewing vector v, ambient
	// light Ia, diffuse and specular light Ids
	// return value is a new Color object
	private Color phongIlluminate(float[] material, float materialN, float[] l, float[] n, float[] v, float[] Ia,
			float[] Ids) {
		float ir = 0, ig = 0, ib = 0; // reflected intensity, rgb channels
		float[] r = new float[3]; // reflection vector
		float ln, rv; // scalar products <l,n> and <r,v>

		// <l,n>

		// ambient component, Ia*ra
//	ir += 
//	ig += 
//	ib += 
//
//	// diffuse component, Ids*rd*<l,n>
//	if (ln > 0) {
//	    ir += 
//	    ig += 
//	    ib += 
//
//	    // reflection vector r=2*<l,n>*n-l
//	    r[0] = 
//	    r[1] = 
//	    r[2] = 
//
//	    // <r,v>
//	    rv = 
//
//	    // specular component, Ids*rs*<r,v>^n
//	    if (rv > 0) {
//		float pow = 
//		ir += 
//		ig += 
//		ib += 
//	    }
//	}

		// System.out.println(ir+" "+ig+" "+ib);
//	return new Color();
		return null;
	}

	//TODO: Normalisieren
	// vector normalization
	// CAUTION: vec is an in-/output parameter; the referenced object will be
	// altered!
	private float normalize(float[] vec) {
		float l = 1.0f;

		return l;
	}

	public static void main(String[] args) {
		Raytracer00 rt = new Raytracer00();

		// rt.doRayTrace();
	}
}
