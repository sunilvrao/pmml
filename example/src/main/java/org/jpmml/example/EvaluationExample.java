/*
 * Copyright (c) 2011 University of Tartu
 */
package org.jpmml.example;

import java.io.*;
import java.util.*;

import org.jpmml.evaluator.*;
import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class EvaluationExample {

	static
	public void main(String... args) throws Exception {

		if(args.length != 1){
			System.err.println("Usage: java " + EvaluationExample.class.getName() + " <PMML file>");

			System.exit(-1);
		}

		File pmmlFile = new File(args[0]);

		PMML pmml = IOUtil.unmarshal(pmmlFile);

		evaluate(pmml);
	}

	static
	public void evaluate(PMML pmml) throws Exception {
		PMMLManager pmmlManager = new PMMLManager(pmml);

		// Load the default model
		Evaluator evaluator = (Evaluator)pmmlManager.getModelManager(null, ModelEvaluatorFactory.getInstance());

		Map<FieldName, ?> parameters = readParameters(evaluator);

		Object result = evaluator.evaluate(parameters);

		System.out.println("Model output: " + result);
	}

	static
	public Map<FieldName, ?> readParameters(Evaluator evaluator) throws IOException {
		Map<FieldName, Object> parameters = new LinkedHashMap<FieldName, Object>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		try {
			List<FieldName> names = evaluator.getActiveFields();
			System.out.println("Model input " + names.size() + " parameter(s):");

			for(int i = 0; i < names.size(); i++){
				FieldName name = names.get(i);

				DataField dataField = evaluator.getDataField(name);
				System.out.print("#" + (i + 1) + " (displayName=" + dataField.getDisplayName() + ", dataType=" + dataField.getDataType()+ "): ");

				String input = reader.readLine();
				if(input == null){
					throw new EOFException();
				}

				parameters.put(name, ParameterUtil.parse(dataField, input));
			}
		} finally {
			reader.close();
		}

		return parameters;
	}
}