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

			//gui.addObject(RTFileReader.read(I_Sphere.class, new File("data/ikugel.dat")));
			gui.addObject(RTFileReader.read(I_Sphere.class, new File("data/dreieck1.dat")));

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

		// viewing vector at intersection point
		v[0] = -rayVx;
		v[1] = -rayVy;
		v[2] = -rayVz;
		normalize(v);

		RTFile scene;
		I_Sphere sphere;
		T_Mesh mesh;

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

				// prepare everything for phong shading
				// the intersection point
				minIP[0] = (float) (rayEx + minT * rayVx);
				minIP[1] = (float) (rayEy + minT * rayVy);
				minIP[2] = (float) (rayEz + minT * rayVz);

				// the normal vector at the intersection point
				minN[0] = minIP[0] - sphere.center[0];
				minN[1] = minIP[1] - sphere.center[1];
				minN[2] = minIP[2] - sphere.center[2];
				normalize(minN);

				// the material
				minMaterial = sphere.material;
				minMaterialN = sphere.materialN;

			} else if (scene instanceof T_Mesh) {
				mesh = (T_Mesh) scene;

				float t;
				float[] n;
				float[] ip = new float[3];

				float a, rayVn, pen;
				float[] p1, p2, p3;
				float[] ai = new float[3];

				// loop over all triangles
				for (int i = 0; i < mesh.triangles.length; i++) {
				    // get the three vertices
				    p1 = mesh.vertices[mesh.triangles[i][0]];
				    p2 = mesh.vertices[mesh.triangles[i][1]];
				    p3 = mesh.vertices[mesh.triangles[i][2]];

				    // intermediate version
				    // calculate normal n and triangle area a
				    // a = 1/2 * |n| wobei n vector
				    n = new float[3];
				    n[0] = p2[1] * p1[2] - p2[2] * p1[1];
				    n[1] = p2[2] * p1[0] - p2[0] * p1[2];
				    n[2] = p2[0] * p1[1] - p2[1] * p1[0];
				    float ln = normalize(n);
				    a = 1/2 * ln;

				    //????? / länge von rayV und n ??
				    rayVn = rayVx * n[0] + rayVy * n[1] + rayVz * n[2];

				    // backface? => next triangle
				    if (rayVn >= 0)
					continue;	    

				    // no intersection point? => next triangle
				    if (Math.abs(rayVn) < 1E-7)
					continue;

				    pen = 

				    // calculate intersection point with plane along the ray
				    t = 

				    // already a closer intersection point? => next triangle
				    if (t >= minT)
					continue;

				    // the intersection point with the plane
				    ip[0] = 
				    ip[1] = 
				    ip[2] = 

				    // no intersection point with the triangle? => next
				    // triangle
				    if (!triangleTest(ip, p1, p2, p3, a, ai))
					continue;

				    // from here: t < minT and triangle intersection
				    // I'm the winner until now!

				    minT = t;
				    minObjectsIndex = objectsNumber;
				    minIndex = i;

				    // prepare everything for shading alternatives

				    // the intersection point
				    minIP[0] = ip[0];
				    minIP[1] = ip[1];
				    minIP[2] = ip[2];

				    switch (mesh.fgp) {
				    case 'f':
				    case 'F':
					
					 // the normal is the surface normal 
					 minN[0] = n[0];
					 minN[1] = n[1]; 
					 minN[2] = n[2];
					  
					 // the material is the material of the first triangle point 
					 int matIndex = mesh.verticesMat[mesh.triangles[minIndex][0]];
					 minMaterial = mesh.materials[matIndex]; 
					 minMaterialN= mesh.materialsN[matIndex];
					
					break;
				    }
				}
			} else 
				continue;
		}

		// no intersection point found => return with no result
		if (minObjectsIndex == -1)
			return null;

		// light vector at the intersection point
		l[0] = this.ICenter[0] - minIP[0];
		l[1] = this.ICenter[1] - minIP[1];
		l[2] = this.ICenter[2] - minIP[2];
		normalize(l);

		// decide which shading model will be applied
		// implicit: only phong shading available => shade=illuminate
		if (objects.get(minObjectsIndex) instanceof I_Sphere)
			return phongIlluminate(minMaterial, minMaterialN, l, minN, v, Ia, Ids);
		else if(objects.get(minObjectsIndex).getHeader() == "TRIANGLE_MESH") {
			mesh = ((T_Mesh) objects.get(minObjectsIndex));
		    switch (mesh.fgp) {
		    case 'f':
		    case 'F':
			// illumination can be calculated here
			// this is a variant between flat und phong shading
			return phongIlluminate(minMaterial, minMaterialN, l, minN, v, Ia, Ids);
		    }
		}

		return null;

		// intermediate version
		// Random rd = new Random();
		// return new Color(rd.nextFloat(), rd.nextFloat(), rd.nextFloat());

	}


	// calculate phong illumination model with material parameters material and
	// materialN, light vector l, normal vector n, viewing vector v, ambient
	// light Ia, diffuse and specular light Ids
	// return value is a new Color object
	private Color phongIlluminate(float[] material, float materialN, float[] l, float[] n, float[] v, float[] Ia, float[] Ids) {
		float ir = 0, ig = 0, ib = 0; // reflected intensity, rgb channels
		float[] r = new float[3]; // reflection vector
		float ln, rv; // scalar products <l,n> and <r,v>

		// <l,n>
		ln = l[0] * n[0] + l[1] * n[1] + l[2] * n[2];

		// ambient component, Ia*ra
		ir += Ia[0] * material[0];
		ig += Ia[1] * material[1];
		ib += Ia[2] * material[2];

		// diffuse component, Ids*rd*<l,n>
		if (ln > 0) {
			ir += Ids[0] * material[3] * ln;
			ig += Ids[1] * material[4] * ln;
			ib += Ids[2] * material[5] * ln;

			// reflection vector r=2*<l,n>*n-l
			r[0] = 2.0f * ln * n[0] - l[0];
			r[1] = 2.0f * ln * n[1] - l[1];
			r[2] = 2.0f * ln * n[2] - l[2];
			normalize(r);

			// <r,v>
			rv = r[0] * v[0] + r[1] * v[1] + r[2] * v[2];

			// specular component, Ids*rs*<r,v>^n
			if (rv > 0) {
				float pow = (float) Math.pow(rv, materialN);
				ir += Ids[0] * material[6] * pow;
				ig += Ids[1] * material[7] * pow;
				ib += Ids[2] * material[8] * pow;
			}
		}

		// System.out.println(ir+" "+ig+" "+ib);
		return new Color(ir>1?1:ir, ig>1?1:ig, ib>1?1:ib);
	}
	
    // calculate normalized face normal fn of the triangle p1, p2 and p3
    // the return value is the area of triangle
    // CAUTION: fn is an output parameter; the referenced object will be
    // altered!
    private float calculateN(float[] fn, float[] p1, float[] p2, float[] p3) {
	float ax, ay, az, bx, by, bz;

	// a = Vi2-Vi1, b = Vi3-Vi1


	
	
	

	// n =a x b


	

	// normalize n, calculate and return area of triangle
	return normalize(fn) / 2;
    }

	// vector normalization
	// CAUTION: vec is an in-/output parameter; the referenced object will be
	// altered!
	private float normalize(float[] vec) {
		float l = (float) Math.sqrt(vec[0] * vec[0] + vec[1] * vec[1] + vec[2] * vec[2]);

		vec[0] = vec[0] / l;
		vec[1] = vec[1] / l;
		vec[2] = vec[2] / l;

		return l;
	}
	
    // calculate triangle test
    // is p (the intersection point with the plane through p1, p2 and p3) inside
    // the triangle p1, p2 and p3?
    // the return value answers this question
    // a is an input parameter - the given area of the triangle p1, p2 and p3
    // ai will be computed to be the areas of the sub-triangles to allow to
    // compute barycentric coordinates of the intersection point p
    // ai[0] is associated with bu (p1p2p) across from p3
    // ai[1] is associated with bv (pp2p3) across from p1
    // ai[2] is associated with bw (p1pp3) across form p2
    // CAUTION: ai is an output parameter; the referenced object will be
    // altered!
    private boolean triangleTest(float[] p, float[] p1, float[] p2, float[] p3, float a, float ai[]) {
	float tmp[] = new float[3];

	//ai[0] = 
	//ai[1] = 
	//ai[2] = 

	//if 
	//    return true;

	return false;
    }

	public static void main(String[] args) {
		Raytracer00 rt = new Raytracer00();

		// rt.doRayTrace();
	}
}
