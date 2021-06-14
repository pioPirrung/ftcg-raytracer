package de.hskl.imst.i.cgma.raytracer.file;
 
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class T_Mesh extends RT_Object {
    // read information
	public float[][] materials;
	public int[] materialsN;
	
	public float[][] vertices;
	public int[] verticesMat;
	
	public int[][] triangles;
	
	public char fgp='f';  // flat, gouraud, phong
	
	// calculated information
    public float[][] vertexNormals;
    public float[][] vertexColors; 
    
    public float[][] triangleNormals;
    public float[][] triangleColors;
    public float[] triangleAreas;
	
	@Override
	public String getHeader() {
		return "TRIANGLE_MESH";
	}
	

	@Override
	public void readContent(LineNumberReader br) throws IOException {
		// dateiinformationen lesen
		Pattern pInfo = Pattern.compile(fInfoRegex);
		Pattern pMaterial = Pattern.compile(materialRegex);
		Pattern pVertex = Pattern.compile(vertexRegex);
		Pattern pTriangle = Pattern.compile(triangleRegex);
		Matcher matcher = pInfo.matcher(readLine(br));
		if(!matcher.matches())
			throw new IOException("Ungültiges Dateiformat!");
		int nExpVerts, nExpTriangles, nExpMaterials;
		nExpVerts = Integer.parseInt(matcher.group(1));
		nExpTriangles = Integer.parseInt(matcher.group(2));
		nExpMaterials = Integer.parseInt(matcher.group(3));
		fgp=matcher.group(4).charAt(0);
		materials = new float[nExpMaterials][9];	// ar ag ab dr dg db sr sg sb
		materialsN = new int[nExpMaterials];		// n
		vertices = new float[nExpVerts][3];		// x y z
		verticesMat = new int[nExpVerts];			// Materialindex
		triangles = new int[nExpTriangles][3];		// i1 i2 i3
		
		// Materialien lesen
		for(int i = 0; i < nExpMaterials; ++i) {
			matcher = pMaterial.matcher(readLine(br).trim());
			if(!matcher.matches()) {
				throw new IOException("Ungültiges Dateiformat! " + br.getLineNumber());
			}

			for(int j = 0; j < 9; ++j)
				materials[i][j] = Float.parseFloat(matcher.group(j+1));
			materialsN[i] = Integer.parseInt(matcher.group(10));
		}
		
		// Vertices lesen
		for(int i = 0; i < nExpVerts; i++) {
			matcher = pVertex.matcher(readLine(br).trim());
			if(!matcher.matches())
				throw new IOException("Ungültiges Dateiformat! " + br.getLineNumber());
			
			for(int j = 0; j < 3; ++j)
			    vertices[i][j] = Float.parseFloat(matcher.group(1+j));
			verticesMat[i] = Integer.parseInt(matcher.group(4));
		}
		
		// Dreiecke lesen
		for(int i = 0; i < nExpTriangles; i++) {
			matcher = pTriangle.matcher(readLine(br).trim());
			if(!matcher.matches())
				throw new IOException("Ungültiges Dateiformat! " + br.getLineNumber());
			
			for(int j = 0; j < 3; ++j)
				triangles[i][j] = Integer.parseInt(matcher.group(j+1));
		}
		
		// BBox berechnen
		calcBoundingBox();
	}
	
	@Override
	protected void readObjContent(LineNumberReader objFileReader, LineNumberReader objVertsFileReader, LineNumberReader materialFileReader) throws IOException {
		String line;
		int nExpVerts, nExpTriangles;
		materials = new float[1][9];	// ambient, diffuse, specular
		materialsN = new int[1];		// n
		fgp = 'p';						// using phong as default
		
		//From here parse material data from mtl file. To know which color the object will have
		if(materialFileReader!=null){
			try {
				//try to read lines of the file
				// ka = ambient
				// kd = diffuse
				// ks = specular
				// ns = n
				while((line = materialFileReader.readLine()) != null) {
					if(line.startsWith("Ka")){
						String[] str=line.split("[ ]+");
						materials[0][0] = Float.parseFloat(str[1]);
						materials[0][1] = Float.parseFloat(str[2]);
						materials[0][2] = Float.parseFloat(str[3]);
					}
					else
					if(line.startsWith("Kd")){
						String[] str=line.split("[ ]+");
						materials[0][3] = Float.parseFloat(str[1]);
						materials[0][4] = Float.parseFloat(str[2]);
						materials[0][5] = Float.parseFloat(str[3]);
					}
					else
					if(line.startsWith("Ks")){
						String[] str=line.split("[ ]+");
						materials[0][6] = Float.parseFloat(str[1]);
						materials[0][7] = Float.parseFloat(str[2]);
						materials[0][8] = Float.parseFloat(str[3]);
					}
					else
					if(line.startsWith("Ns")){
						String[] str = line.split("[ ]+");
						materialsN[0] = Integer.parseInt(str[1]);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Ungültiges Dateiformat!");
			}
		}
		
		//From here arse obj data from obj file. For this we need to know how much vertices and triangle faces the obj file has.
		//We need this to initialize the arrays!
		nExpVerts = 0;
		nExpTriangles = 0;
		
		if(objFileReader!=null){
			try {
				//try to read lines of the file
				// v but NOT vn means the amount of vertices in the obj file
				// f means the triangle faces in obj file
				while((line = objFileReader.readLine()) != null) {
					if(line.startsWith("v") && !line.startsWith("vn"))
						nExpVerts += 1;
					
					if(line.startsWith("f"))
						nExpTriangles += 1;
				}
				System.out.println(nExpVerts);
				System.out.println(nExpTriangles);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Ungültiges Dateiformat!");
			}
		}
		
		//initialize arrays for vertices and triangle faces data
		vertices = new float[nExpVerts][3];
		verticesMat = new int[nExpVerts];
		triangles = new int[nExpTriangles][3];
		
		if(objVertsFileReader!=null){
			try {
				int nVertsTemp = 0;
				int nTrianglesTemp = 0;
				//try to read lines of the file
				//v but NOT vn means vertices, store correct vertices in vertices array
				//verticesMat is always 0 because the whole obj only has one color!
				//f store triangle faces in triangles array. Check processFLine method for further informations.
				while((line = objVertsFileReader.readLine()) != null) {
					if(line.startsWith("v") && !line.startsWith("vn")) {
						String[] str=line.split("[ ]+");
						for(int i = 1; i < str.length; i++)
							vertices[nVertsTemp][i-1] = Float.parseFloat(str[i]);
						verticesMat[nVertsTemp] = 0;
						nVertsTemp += 1;
					}
						
					if(line.startsWith("f")) {
						processFLine(line, nTrianglesTemp, triangles);
						nTrianglesTemp += 1;
					}
						
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new IOException("Ungültiges Dateiformat!");
			}
		}
		
		// BBox berechnen
		calcBoundingBox();
	}
	
	/**
	 * 
	 * @param line Complete line of the file as String
	 * @param nTriangles Current triangle index
	 * @param triangles triangle array where to store triangle faces
	 * 
	 * There are 4 possibility's an obj file can store triangle faces. Check which one is used and parse the vertex data from the faces.
	 */
	private void processFLine(String line, int nTriangles, int[][] triangles) {
		String [] tokens=line.split("[ ]+");
		int c=tokens.length;

		if(tokens[1].matches("[0-9]+")){
			if(c==4){//3 faces
				for(int i=1; i<c; i++) 
					triangles[nTriangles][i-1] = Integer.parseInt(tokens[i]) - 1;
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+")){
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					String str = tokens[i].split("/")[0];
					if(Integer.parseInt(str) >= verticesMat.length)
						triangles[nTriangles][i-1] = Integer.parseInt(str) - 1;
				}
			}
		}
		if(tokens[1].matches("[0-9]+//[0-9]+")){
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					String str = tokens[i].split("//")[0];
					triangles[nTriangles][i-1] = Integer.parseInt(str) - 1;
				}
			}
		}
		if(tokens[1].matches("[0-9]+/[0-9]+/[0-9]+")){
			if(c==4){//3 faces
				for(int i=1; i<c; i++){
					String str = tokens[i].split("/")[0];
					triangles[nTriangles][i-1] = Integer.parseInt(str);
				}
			}
		}
	}
	
	@Override
	public void calcBoundingBox() {
		this.min[0] = this.vertices[0][0];
		this.min[1] = this.vertices[0][1];
		this.min[2] = this.vertices[0][2];
		this.max[0] = this.vertices[0][0];
		this.max[1] = this.vertices[0][1];
		this.max[2] = this.vertices[0][2];
		
		for(int i = 1; i < this.vertices.length; i++) {
			for(int j = 0; j < 3; j++) {
				if(this.vertices[i][j] < this.min[j])
					this.min[j] = this.vertices[i][j];
				
				if(this.vertices[i][j] > this.max[j])
					this.max[j] = this.vertices[i][j];
			}
		}
	}
	
	private static final String fInfoRegex =
			"([0-9]*) ([0-9]*) ([0-9]*) ([fgpFGP])";
	private static final String materialRegex =
			"(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +([0-9]+)";
	private static final String vertexRegex =
			"(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) +(\\-?[0-9]+\\.[0-9]+) ([0-9]+)";
	private static final String triangleRegex =
			"([0-9]+) +([0-9]+) +([0-9]+)";
}
