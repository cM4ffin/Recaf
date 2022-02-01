package me.coley.recaf.decompile.fallback.print;

import me.coley.recaf.decompile.fallback.model.ClassModel;
import me.coley.recaf.decompile.fallback.model.MethodModel;
import me.coley.recaf.util.AccessFlag;
import me.coley.recaf.util.StringUtil;
import org.objectweb.asm.Type;

import java.util.Collection;

/**
 * Method printing strategy for normal methods.
 *
 * @author Matt Coley
 */
public class BasicMethodPrintStrategy implements MethodPrintStrategy {
	@Override
	public String print(ClassModel parent, MethodModel model) {
		Printer out = new Printer();
		// TODO: Annotations
		appendDeclaration(out, model);
		// TODO: Handle abstract not printing body
		appendBody(out, model);
		return out.toString();
	}

	/**
	 * Appends the method declaration to the printer.
	 * <ol>
	 *     <li>{@link #buildDeclarationFlags(StringBuilder, MethodModel)}</li>
	 *     <li>{@link #buildDeclarationReturnType(StringBuilder, MethodModel)}</li>
	 *     <li>{@link #buildDeclarationName(StringBuilder, MethodModel)}</li>
	 *     <li>{@link #buildDeclarationArgs(StringBuilder, MethodModel)}</li>
	 * </ol>
	 *
	 * @param out
	 * 		Printer to write to.
	 * @param model
	 * 		Model to pull info from.
	 */
	protected void appendDeclaration(Printer out, MethodModel model) {
		StringBuilder sb = new StringBuilder();
		buildDeclarationFlags(sb, model);
		buildDeclarationReturnType(sb, model);
		buildDeclarationName(sb, model);
		buildDeclarationArgs(sb, model);
		// TODO: Throws list
		out.appendLine(sb.toString());
	}

	/**
	 * Appends the method body to the printer.
	 *
	 * @param out
	 * 		Printer to write to.
	 * @param model
	 * 		Model to pull info from.
	 */
	protected void appendBody(Printer out, MethodModel model) {
		out.appendMultiLine("{\n" +
				"    throw new RuntimeException(\"Stub method\");\n" +
				"}" + Printer.FORCE_NEWLINE);
	}

	/**
	 * Appends the following pattern to the builder:
	 * <pre>
	 * public static abstract...
	 * </pre>
	 *
	 * @param sb
	 * 		Builder to add to.
	 * @param model
	 * 		Model to pull info from.
	 *
	 * @see #appendDeclaration(Printer, MethodModel) parent caller
	 */
	protected void buildDeclarationFlags(StringBuilder sb, MethodModel model) {
		Collection<AccessFlag> flags = AccessFlag.getApplicableFlags(AccessFlag.Type.METHOD, model.getAccess());
		flags = AccessFlag.sort(AccessFlag.Type.METHOD, flags);
		if (!flags.isEmpty()) {
			sb.append(AccessFlag.toString(flags)).append(' ');
		}
	}

	/**
	 * Appends the following pattern to the builder:
	 * <pre>
	 * ReturnType
	 * </pre>
	 *
	 * @param sb
	 * 		Builder to add to.
	 * @param model
	 * 		Model to pull info from.
	 *
	 * @see #appendDeclaration(Printer, MethodModel) parent caller
	 */
	protected void buildDeclarationReturnType(StringBuilder sb, MethodModel model) {
		Type methodType = Type.getMethodType(model.getDesc());
		String returnTypeName = methodType.getReturnType().getClassName();
		if (returnTypeName.contains("."))
			returnTypeName = returnTypeName.substring(returnTypeName.lastIndexOf(".") + 1);
		sb.append(returnTypeName).append(' ');
	}

	/**
	 * Appends the following pattern to the builder:
	 * <pre>
	 * methodName
	 * </pre>
	 *
	 * @param sb
	 * 		Builder to add to.
	 * @param model
	 * 		Model to pull info from.
	 *
	 * @see #appendDeclaration(Printer, MethodModel) parent caller
	 */
	protected void buildDeclarationName(StringBuilder sb, MethodModel model) {
		sb.append(model.getName());
	}

	/**
	 * Appends the following pattern to the builder:
	 * <pre>
	 * (Type argName, Type argName)
	 * </pre>
	 *
	 * @param sb
	 * 		Builder to add to.
	 * @param model
	 * 		Model to pull info from.
	 *
	 * @see #appendDeclaration(Printer, MethodModel) parent caller
	 */
	protected void buildDeclarationArgs(StringBuilder sb, MethodModel model) {
		sb.append('(');
		boolean isVarargs = AccessFlag.isVarargs(model.getAccess());
		int varIndex = AccessFlag.isStatic(model.getAccess()) ? 0 : 1;
		Type methodType = Type.getMethodType(model.getDesc());
		Type[] argTypes = methodType.getArgumentTypes();
		for (int param = 0; param < argTypes.length; param++) {
			// Get arg type text
			Type argType = argTypes[param];
			String argTypeName = argType.getClassName();
			if (argTypeName.contains("."))
				argTypeName = argTypeName.substring(argTypeName.lastIndexOf(".") + 1);
			boolean isLast = param == argTypes.length - 1;
			if (isVarargs && isLast && argType.getSort() == Type.ARRAY) {
				argTypeName = StringUtil.replaceLast(argTypeName, "[]", "...");
			}
			// Get arg name
			//  - TODO: Pull from variable table (if valid names)
			//     - Can lean on assembler and yoink 'em outta MethodDefinition
			String name = "p" + varIndex;
			// Append to arg list
			sb.append(argTypeName).append(' ').append(name);
			if (!isLast) {
				sb.append(", ");
			}
			// Increment for next var
			varIndex += argType.getSize();
		}
		sb.append(')');
	}
}
