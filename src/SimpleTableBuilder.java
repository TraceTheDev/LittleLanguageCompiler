import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class SimpleTableBuilder extends LittleBaseListener {
	private Stack<Map<String, String>> symbolTables; // Stack of symbol tables
	private int tempCounter; // Counter for temporary variables
	private int regCounter; // Counter for registers
	private List<String> writeVariables; // List to store variables for output
	private List<String> readVariables;
	private List<String> moveVariables;
	private List<String> mulVariables;
	private List<String> stoVariables;
	private List<String> addVariables;
	private List<String> subVariables;
	private List<String> divVariables;
	private List<String> constants;

	public SimpleTableBuilder() {
		symbolTables = new Stack<>();
		symbolTables.push(new HashMap<>()); // Initialize global symbol table
		writeVariables = new ArrayList<>(); // Initialize list for output variables
		readVariables = new ArrayList<>();
		moveVariables = new ArrayList<>();
		mulVariables = new ArrayList<>();
		constants = new ArrayList<>();
		stoVariables = new ArrayList<>();
		addVariables = new ArrayList<>();
		subVariables = new ArrayList<>();
		divVariables = new ArrayList<>();
	}

	@Override
	public void enterProgram(LittleParser.ProgramContext ctx) {
		System.out.println(";IR code");
		System.out.println(";LABEL main");
		System.out.println(";LINK");
	}

	@Override
	public void enterWrite_stmt(LittleParser.Write_stmtContext ctx) {
		String[] expressions = ctx.id_list().getText().split(",");
		for (String expression : expressions) {
			Map<String, String> currentSymbolTable = symbolTables.peek();
			String variableType = currentSymbolTable.get(expression);
			boolean isFloat = variableType != null && variableType.equals("FLOAT");

			if (expression.equals("newline")) {
				System.out.println(";WRITES newline");
				writeVariables.add(expression);
			} else {
				System.out.println(";WRITE" + (isFloat ? "F " : "I ") + expression);
				writeVariables.add(expression); // Store written variables
			}
		}
	}

	@Override
	public void enterRead_stmt(LittleParser.Read_stmtContext ctx) {
		String[] variables = ctx.id_list().getText().split(",");
		for (String variable : variables) {
			Map<String, String> currentSymbolTable = symbolTables.peek();
			String variableType = currentSymbolTable.get(variable);
			boolean isFloat = variableType != null && variableType.equals("FLOAT");
			System.out.println(";READ" + (isFloat ? "F " : "I ") + variable);
			readVariables.add(variable); // Store read variables
		}
	}

	@Override
	public void enterAssign_stmt(LittleParser.Assign_stmtContext ctx) {
		String assignmentExpr = ctx.assign_expr().getText().trim(); // Get the whole assignment expression
		String[] parts = assignmentExpr.split(":="); // Split the assignment expression into variable and value

		if (parts.length == 2) { // Ensure the assignment expression has both variable and value parts
			String variable = parts[0].trim(); // Variable name
			String value = parts[1].trim(); // Assigned value
			Map<String, String> currentSymbolTable = symbolTables.peek();
			String variableType = currentSymbolTable.get(variable);
			boolean isFloat = variableType != null && variableType.equals("FLOAT");

			// Generating IR code for assignment
			String processedValue = processExpression(value); // Process the value to replace the last operation label
			if (value.contains("*")) {
				System.out.println(";MULT" + (isFloat ? "F " : "I ") + processedValue + " $T" + ++tempCounter);
				mulVariables.add(processedValue);
			} else if (value.contains("+")) {
				System.out.println(";ADD" + (isFloat ? "F " : "I ") + processedValue + " $T" + ++tempCounter);
				addVariables.add(processedValue);
			} else if (value.contains("-")) {
				System.out.println(";SUB" + (isFloat ? "F " : "I ") + processedValue + " $T" + ++tempCounter);
				subVariables.add(processedValue);
			} else if (value.contains("/")) {
				String[] divParts = value.split("/");
				String denominator = divParts[1].trim();
				if (denominator.contains(".")) {
					System.out.println(";STORE" + (isFloat ? "F " : "I ") + denominator + " $T" + ++tempCounter);
				}
				System.out.println(";DIV" + (isFloat ? "F " : "I ") + processedValue + " $T" + ++tempCounter);
				divVariables.add(processedValue);
			} else {
				System.out.println(";STORE" + (isFloat ? "F " : "I ") + processedValue + " $T" + ++tempCounter);
				constants.add(processedValue);
				moveVariables.add(variable);
			}
			System.out.println(";STORE" + (isFloat ? "F " : "I ") + "$T" + tempCounter + " " + variable);
			stoVariables.add(variable);
		} else {
			// Handle invalid assignment expressions
			System.err.println("Error: Invalid assignment expression: " + assignmentExpr);
		}
	}

	@Override
	public void enterVar_decl(LittleParser.Var_declContext ctx) {
		String[] names = ctx.id_list().getText().split(",");
		for (String name : names) {
			symbolTables.peek().put(name.trim(), ctx.var_type().getText());
		}
	}

	@Override
	public void enterString_decl(LittleParser.String_declContext ctx) {
		String[] declarations = ctx.getText().split(",");
		for (String declaration : declarations) {
			String[] parts = declaration.trim().split(":=");
			String variableName = parts[0].trim();
			String value = parts[1].trim();
			if (value.endsWith(";")) {
				value = value.substring(0, value.length() - 1); // Remove semicolon from end of string
			}
			symbolTables.peek().put(variableName, value);
		}
	}

	private String processExpression(String expression) {
		// Replace multiplication and addition symbols with proper formatting
		expression = expression.replaceAll("\\*", " ");
		expression = expression.replaceAll("\\+", " ");
		expression = expression.replaceAll("\\-", " ");
		expression = expression.replaceAll("\\/", " ");
		expression = expression.replaceAll("\\(", "");
		expression = expression.replaceAll("\\)", "");

		return expression;
	}

	public void prettyPrint() {
		System.out.println(";RET");
		System.out.println(";tiny code");

		// Print variable declarations
		for (Map.Entry<String, String> entry : symbolTables.peek().entrySet()) {
			if (!entry.getKey().startsWith("STRING")) {
				System.out.println("var " + entry.getKey());
			} else {
				String variableName = entry.getKey().substring(6);
				String value = entry.getValue().replaceAll("\"", "");
				System.out.println("str " + variableName + " \"" + value + "\"");
			}
		}

		for(String con : constants) {
			String var = moveVariables.get(regCounter);
			System.out.println("move " + con + " r" + regCounter);
			System.out.println("move " + "r" + regCounter++ + " " + var);
		}

		for(String rVariable : readVariables) {
			System.out.println("sys readi " + rVariable);
		}

		for(String subVariable : subVariables) {
			String[] parts = subVariable.trim().split(" ");
			System.out.println("move " + parts[0] + " r" + regCounter);
			System.out.println("subi " + parts[1] + " r" + regCounter);
			System.out.println("move " + "r" + regCounter + " " + stoVariables.get(regCounter));
			regCounter++;
		}

		for(String mulVar : mulVariables) {
			String[] parts = mulVar.trim().split(" ");
			System.out.println("move " + parts[0] + " r" + regCounter);
			System.out.println("muli " + parts[1] + " r" + regCounter);
			System.out.println("move " + "r" + regCounter + " " + stoVariables.get(regCounter));
			regCounter++;
		}

		for(String addVar : addVariables) {
			String[] parts = addVar.trim().split(" ");
			System.out.println("move " + parts[0] + " r" + regCounter);
			System.out.println("addi " + parts[1] + " r" + regCounter);
			System.out.println("move " + "r" + regCounter + " " + stoVariables.get(regCounter));
			regCounter++;
		}
      
		for(String divVar : divVariables) {
			String[] parts = divVar.trim().split(" ");
			System.out.println("move " + parts[0] + " r" + regCounter);
			System.out.println("divi " + parts[1] + " r" + regCounter);
			System.out.println("move " + "r" + regCounter + " " + parts[1]);
			regCounter++;
		}

		// Print additional output statements for variables that were read or written
		for (String wVariable : writeVariables) {
			System.out.println("sys writei " + wVariable);
		}

		// Print halt statement
		System.out.println("sys halt");
	}

}
