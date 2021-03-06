package ch.zhaw.ocr.nn.helper;

import hu.kazocsaba.math.matrix.Matrix;
import hu.kazocsaba.math.matrix.MatrixFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.jungblut.math.DoubleVector;
import de.jungblut.math.dense.DenseDoubleVector;

/**
 * Various functions used when working with matrices
 * @author Corinne Zeugin, Priscilla Schneider, Adrian Schmid
 */
public class MatrixHelper {

	/**
	 * Element-by-element multiplication of two matrices
	 * 
	 * @param m1
	 *            input matrix one
	 * @param m2
	 *            input matrix two
	 * @return result of element-by-element multiplication
	 * @throws IllegalArgumentException
	 */
	public static Matrix elementMultiplication(Matrix m1, Matrix m2)
			throws IllegalArgumentException {
		if (m1.getColumnCount() != m2.getColumnCount()
				|| m1.getRowCount() != m2.getRowCount()) {
			throw new IllegalArgumentException("Matrix dimensions must agree!");
		}

		Matrix result = MatrixFactory.createLike(m1);

		for (int col = 0; col < m1.getColumnCount(); col++) {
			for (int row = 0; row < m1.getRowCount(); row++) {
				result.set(row, col, m1.get(row, col) * m2.get(row, col));
			}
		}

		return result;
	}

	/**
	 * Use sigmoid function on each element of a given matrix
	 * 
	 * @param m
	 *            input matrix
	 * @return transformed matrix
	 */
	public static Matrix sigmoid(Matrix m) {
		m = MatrixFactory.copy(m);
		for (int col = 0; col < m.getColumnCount(); col++) {
			for (int row = 0; row < m.getRowCount(); row++) {
				m.set(row, col, 1 / (1 + Math.exp(-1 * m.get(row, col))));
			}
		}
		return m;
	}

	/**
	 * Calculates the sigmoid gradient of a matrix (sigmoid(m) .*
	 * (ones-sigmoind(m))
	 * 
	 * @param m
	 *            input matrix
	 * @return sigmoid gradient of matrix m
	 */
	public static Matrix sigmoidGradient(Matrix m) {
		Matrix ones = MatrixFactory.ones(m.getRowCount(), m.getColumnCount());
		Matrix sig = sigmoid(m);
		return MatrixHelper.elementMultiplication(sig, ones.minus(sig));
	}

	/**
	 * Add 1 to a vector (similar to [1 m] or [1;m]
	 * @param vector input vector
	 * @param horizontalVertical declare wheter [1 m] or [1;m] should be applied. "horizontal" => [1 m]; "vertical => [1;m]
	 * @return new vector with added one
	 */
	public static Matrix add1ToVector(Matrix vector, String horizontalVertical) {
		vector = MatrixFactory.copy(vector);
		if (horizontalVertical.equals("horizontal")) {
			Matrix rv = MatrixFactory.createMatrix(1,
					vector.getColumnCount() + 1);
			rv.set(0, 0, 1);
			rv.setSubmatrix(vector, 0, 1);

			return rv;
		} else if (horizontalVertical.equals("vertical")) {
			Matrix rv = MatrixFactory.createMatrix(vector.getRowCount() + 1, 1);
			rv.set(0, 0, 1);
			rv.setSubmatrix(vector, 1, 0);

			return rv;
		} else {
			throw new IllegalArgumentException(
					"use either 'horizontal' or 'vertical' to determine the matrix type");
		}
	}

	/**
	 * Uses natural logarithm on all matrix elements
	 * @param m input matrix
	 * @return copy of the input matrix where natural logarithm has been applied on all elements
	 */
	public static Matrix log(Matrix m) {
		m = MatrixFactory.copy(m);
		for (int col = 0; col < m.getColumnCount(); col++) {
			for (int row = 0; row < m.getRowCount(); row++) {
				m.set(row, col, Math.log(m.get(row, col)));
			}
		}
		return m;
	}

	/**
	 * Add a scalar to all values of a matrix
	 * @param m input matrix
	 * @param scalar scalar to be added
	 * @return copy of input matrix + scalar
	 */
	public static Matrix addScalar(Matrix m, double scalar) {
		m = MatrixFactory.copy(m);
		for (int col = 0; col < m.getColumnCount(); col++) {
			for (int row = 0; row < m.getRowCount(); row++) {
				m.set(row, col, m.get(row, col) + scalar);
			}
		}
		return m;
	}

	/**
	 * Add up all elements of a given matrix
	 * 
	 * @param m matrix to be summed up
	 * @return sum of all matrix values
	 */
	public static double sum(Matrix m) {
		double rv = 0;
		for (int col = 0; col < m.getColumnCount(); col++) {
			for (int row = 0; row < m.getRowCount(); row++) {
				rv += m.get(row, col);
			}
		}
		return rv;
	}

	/**
	 * Get the max element of a matrix
	 * 
	 * @param m matrix to be analysed
	 * @return max element of the matrix
	 */
	public static double max(Matrix m) {
		double max = 0;
		for (int col = 0; col < m.getColumnCount(); col++) {
			for (int row = 0; row < m.getRowCount(); row++) {
				if (m.get(row, col) > max) {
					max = m.get(row, col);
				}
			}
		}
		return max;
	}

	/**
	 * Merge thetas into one vector. Equivalent to octave code [theta1(:);theta2(:)]
	 * 
	 * @param theta1 inputmatrix theta1
	 * @param theta2 inputmatrix theta2
	 * @return vector containing all values of theta 1 + theta 2
	 */
	public static Matrix mergeThetas(Matrix theta1, Matrix theta2) {
		int len = (theta1.getColumnCount() * theta1.getRowCount())
				+ (theta2.getColumnCount() * theta2.getRowCount());
		Matrix rv = MatrixFactory.createMatrix(len, 1);

		int n = 0;

		// add theta 1
		for (int col = 0; col < theta1.getColumnCount(); col++) {
			for (int row = 0; row < theta1.getRowCount(); row++) {
				rv.set(n, 0, theta1.get(row, col));
				n++;
			}
		}

		// add theta 2
		for (int col = 0; col < theta2.getColumnCount(); col++) {
			for (int row = 0; row < theta2.getRowCount(); row++) {
				rv.set(n, 0, theta2.get(row, col));
				n++;
			}
		}

		return rv;
	}

	/**
	 * Generate theta1 + theta2 from a given merged vector
	 * 
	 * theta1 = matrix with dimensions (hiddenLayerSize, inputLayerSize+1)
	 * theta2 = matrix with dimensions (outputLayerSize, hiddenLayerSize+1
	 * 
	 * @param mergedThetas vector containing all values for theta 1 + theta 2
	 * @param inputLayerSize number of neurons in the input layer
	 * @param hiddenLayerSize number of neurons in the hidden layer
	 * @param outputLayerSize numer of neurons in the output layer
	 * @return an array containing theta1 (unmergeThetas[0]) and theta2
	 *         (unmergeThetas[1])
	 */
	public static Matrix[] unmergeThetas(Matrix mergedThetas,
			int inputLayerSize, int hiddenLayerSize, int outputLayerSize) {
		int[] thetaLen = new int[2];
		thetaLen[0] = hiddenLayerSize * (inputLayerSize + 1);
		thetaLen[1] = outputLayerSize * (hiddenLayerSize + 1);

		if (thetaLen[0] + thetaLen[1] > mergedThetas.getRowCount()) {
			throw new IllegalArgumentException(
					"thetaLen[0] + thetaLen[1] > mergedThetas.getRowCount()");
		}

		Matrix[] rv = new Matrix[2];
		rv[0] = MatrixFactory.createMatrix(hiddenLayerSize, inputLayerSize + 1);
		rv[1] = MatrixFactory
				.createMatrix(outputLayerSize, hiddenLayerSize + 1);

		int n = 0;

		for (int i = 0; i < rv.length; i++) {
			for (int col = 0; col < rv[i].getColumnCount(); col++) {
				for (int row = 0; row < rv[i].getRowCount(); row++) {
					rv[i].set(row, col, mergedThetas.get(n, 0));
					n++;
				}
			}
		}

		return rv;
	}

	/**
	 * Create a double vector from a 1 column matrix. Used for fmincg function
	 * 
	 * @param m
	 *            input matrix
	 * @return output DoubleVector
	 */
	public static DoubleVector convertToDoubleVector(Matrix m) {
		if (m.getColumnCount() > 1) {
			throw new IllegalArgumentException(
					"Only vectors (matrix with one column) may be converted");
		}

		double[] arr = new double[m.getRowCount()];

		for (int row = 0; row < m.getRowCount(); row++) {
			arr[row] = m.get(row, 0);
		}

		return new DenseDoubleVector(arr);
	}

	/**
	 * Convert a DoubleVector into a matrix. Used to process output of fmincg
	 * function
	 * 
	 * @param dv
	 *            input DoubleVector
	 * @return generated matrix
	 */
	public static Matrix convertToMatrix(DoubleVector dv) {
		Matrix m = MatrixFactory.createMatrix(dv.getLength(), 1);

		for (int row = 0; row < dv.getLength(); row++) {
			m.set(row, 0, dv.get(row));
		}

		return m;
	}

	/**
	 * Serialize Matrix into a textfile.
	 * Colums are seperated by , rows by ;
	 * 
	 * 		(n11 n12 n13 n14)
	 * m = 	(n21 n22 n23 n24)
	 * 		(n31 n32 n33 n34)
	 * 
	 * will be stored like: n11,n12,n13,n14;n21,n22,n23,n24;n31,n32,n33,n34
	 * 
	 * @param m matrix to be serialized
	 * @param f target file
	 * @throws IOException
	 */
	public static void serializeMatrix(Matrix m, File f) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		StringBuffer sb = new StringBuffer();

		for (int row = 0; row < m.getRowCount(); row++) {
			sb = new StringBuffer();
			for (int col = 0; col < m.getColumnCount(); col++) {
				sb.append(Double.toString(m.get(row, col)));
				sb.append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			bw.write(sb.toString());
			bw.write(";");
			bw.flush();
		}
	}

	/**
	 * Deserialize Matrix from textfile
	 * 
	 * n11,n12,n13,n14;n21,n22,n23,n24;n31,n32,n33,n34
	 * 
	 * will be converted in to a matrix like 
	 * 
	 * 		(n11 n12 n13 n14)
	 * m = 	(n21 n22 n23 n24)
	 * 		(n31 n32 n33 n34)
	 * 
	 * @param f file containing a serialized matrix (created using the MatrixHelper.serializeMatrix() function)
	 * @return Matrix containing the values of the textfile
	 * @throws IOException
	 */
	public static Matrix deserializeMatrix(File f) throws IOException {
		BufferedReader input = null;
		Matrix m = null;
		
		int r = 0;
		int c = 0;
		
		input = new BufferedReader(new FileReader(f));
		String line = null; // not declared within while loop
		while ((line = input.readLine()) != null) {
			for (String row : line.split(";")) {
				if (!row.trim().isEmpty()) {
					c = 0;
					for(String e : row.split(",")){
						if(m == null){
							m = MatrixFactory.createMatrix(line.split(";").length, row.split(",").length);
						}
						m.set(r, c, Double.valueOf(e));
						c++;
					}
				}
				r++;
			}
		}
		return m;
	}

	
	/**
	 * Checks if 2 matrices have 1. the same dimensions and 2. have the same values in all fields
	 * @param m1 input matrix1
	 * @param m2 input matrix2
	 * @return true if both matrices are the same / false if not
	 */
	public static boolean matrixEquals(Matrix m1, Matrix m2){
		if(m1.getRowCount()!=m2.getRowCount() || m1.getColumnCount()!=m2.getColumnCount()){
			return false;
		}else{
			for(int row = 0;row < m1.getRowCount();row++){
				for(int col = 0; col < m1.getColumnCount();col++){
					if(m1.get(row, col) != m2.get(row, col)){
						return false;
					}
				}
			}
		}
		return true;
	}
}
