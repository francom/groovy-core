/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.ast.tools;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.VariableScope;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.DeclarationExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.FieldExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.classgen.Verifier;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handy methods when working with the Groovy AST
 *
 * @author Guillaume Laforge
 * @author Paul King
 * @author Andre Steingress
 * @author Graeme Rocher
 */
public class GeneralUtils {
    public static final Token ASSIGN = Token.newSymbol(Types.ASSIGN, -1, -1);
    public static final Token EQ = Token.newSymbol(Types.COMPARE_EQUAL, -1, -1);
    public static final Token NE = Token.newSymbol(Types.COMPARE_NOT_EQUAL, -1, -1);
    public static final Token LT = Token.newSymbol(Types.COMPARE_LESS_THAN, -1, -1);
    public static final Token AND = Token.newSymbol(Types.LOGICAL_AND, -1, -1);
    public static final Token OR = Token.newSymbol(Types.LOGICAL_OR, -1, -1);
    public static final Token CMP = Token.newSymbol(Types.COMPARE_TO, -1, -1);
    private static final Token INSTANCEOF = Token.newSymbol(Types.KEYWORD_INSTANCEOF, -1, -1);
    private static final Token PLUS = Token.newSymbol(Types.PLUS, -1, -1);
    private static final Token INDEX = Token.newSymbol("[", -1, -1);

    public static BinaryExpression andX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, AND, rhv);
    }

    public static ArgumentListExpression args(Expression... expressions) {
        List<Expression> args = new ArrayList<Expression>();
        Collections.addAll(args, expressions);
        return new ArgumentListExpression(args);
    }

    public static ArgumentListExpression args(List<Expression> expressions) {
        return new ArgumentListExpression(expressions);
    }

    public static ArgumentListExpression args(Parameter[] parameters) {
        return new ArgumentListExpression(parameters);
    }

    public static ArgumentListExpression args(String... names) {
        List<Expression> vars = new ArrayList<Expression>();
        for (String name : names) {
            vars.add(varX(name));
        }
        return new ArgumentListExpression(vars);
    }

    public static Statement assignS(Expression target, Expression value) {
        return new ExpressionStatement(assignX(target, value));
    }

    public static Expression assignX(Expression target, Expression value) {
        return new BinaryExpression(target, ASSIGN, value);
    }

    public static Expression attrX(Expression oe, Expression prop) {
        return new AttributeExpression(oe, prop);
    }

    public static BlockStatement block(VariableScope varScope, Statement... stmts) {
        BlockStatement block = new BlockStatement();
        block.setVariableScope(varScope);
        for (Statement stmt : stmts) block.addStatement(stmt);
        return block;
    }

    public static BlockStatement block(Statement... stmts) {
        BlockStatement block = new BlockStatement();
        for (Statement stmt : stmts) block.addStatement(stmt);
        return block;
    }

    public static MethodCallExpression callSuperX(String methodName, Expression args) {
        return callX(varX("super"), methodName, args);
    }

    public static MethodCallExpression callSuperX(String methodName) {
        return callSuperX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static MethodCallExpression callThisX(String methodName, Expression args) {
        return callX(varX("this"), methodName, args);
    }

    public static MethodCallExpression callThisX(String methodName) {
        return callThisX(methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static MethodCallExpression callX(Expression receiver, String methodName, Expression args) {
        return new MethodCallExpression(receiver, methodName, args);
    }

    public static MethodCallExpression callX(Expression receiver, Expression method, Expression args) {
        return new MethodCallExpression(receiver, method, args);
    }

    public static MethodCallExpression callX(Expression receiver, String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static StaticMethodCallExpression callX(ClassNode receiver, String methodName, Expression args) {
        return new StaticMethodCallExpression(receiver, methodName, args);
    }

    public static StaticMethodCallExpression callX(ClassNode receiver, String methodName) {
        return callX(receiver, methodName, MethodCallExpression.NO_ARGUMENTS);
    }

    public static CastExpression castX(ClassNode type, Expression expression) {
        return new CastExpression(type, expression);
    }

    public static CastExpression castX(ClassNode type, Expression expression, boolean ignoreAutoboxing) {
        return new CastExpression(type, expression, ignoreAutoboxing);
    }

    public static ClassExpression classX(ClassNode clazz) {
        return new ClassExpression(clazz);
    }

    public static ClassExpression classX(Class clazz) {
        return classX(ClassHelper.make(clazz).getPlainNodeReference());
    }

    public static ClosureExpression closureX(Parameter[] params, Statement code) {
        return new ClosureExpression(params, code);
    }

    public static ClosureExpression closureX(Statement code) {
        return closureX(Parameter.EMPTY_ARRAY, code);
    }

    public static Parameter[] cloneParams(Parameter[] source) {
        Parameter[] result = new Parameter[source.length];
        for (int i = 0; i < source.length; i++) {
            Parameter srcParam = source[i];
            Parameter dstParam = new Parameter(srcParam.getOriginType(), srcParam.getName());
            result[i] = dstParam;
        }
        return result;
    }

    public static BinaryExpression cmpX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, CMP, rhv);
    }

    public static ConstantExpression constX(Object val) {
        return new ConstantExpression(val);
    }

    public static ConstantExpression constX(Object val, boolean keepPrimitive) {
        return new ConstantExpression(val, keepPrimitive);
    }

    /**
     * Copies all <tt>candidateAnnotations</tt> with retention policy {@link java.lang.annotation.RetentionPolicy#RUNTIME}
     * and {@link java.lang.annotation.RetentionPolicy#CLASS}.
     * <p>
     * Annotations with {@link org.codehaus.groovy.runtime.GeneratedClosure} members are not supported at present.
     */
    public static void copyAnnotatedNodeAnnotations(final AnnotatedNode annotatedNode, final List<AnnotationNode> copied, List<AnnotationNode> notCopied) {
        List<AnnotationNode> annotationList = annotatedNode.getAnnotations();
        for (AnnotationNode annotation : annotationList)  {

            List<AnnotationNode> annotations = annotation.getClassNode().getAnnotations(AbstractASTTransformation.RETENTION_CLASSNODE);
            if (annotations.isEmpty()) continue;

            if (hasClosureMember(annotation)) {
                notCopied.add(annotation);
                continue;
            }

            AnnotationNode retentionPolicyAnnotation = annotations.get(0);
            Expression valueExpression = retentionPolicyAnnotation.getMember("value");
            if (!(valueExpression instanceof PropertyExpression)) continue;

            PropertyExpression propertyExpression = (PropertyExpression) valueExpression;
            boolean processAnnotation =
                    propertyExpression.getProperty() instanceof ConstantExpression &&
                            (
                                    "RUNTIME".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue()) ||
                                            "CLASS".equals(((ConstantExpression) (propertyExpression.getProperty())).getValue())
                            );

            if (processAnnotation)  {
                AnnotationNode newAnnotation = new AnnotationNode(annotation.getClassNode());
                for (Map.Entry<String, Expression> member : annotation.getMembers().entrySet())  {
                    newAnnotation.addMember(member.getKey(), member.getValue());
                }
                newAnnotation.setSourcePosition(annotatedNode);

                copied.add(newAnnotation);
            }
        }
    }

    public static Statement createConstructorStatementDefault(FieldNode fNode) {
        final String name = fNode.getName();
        final ClassNode fType = fNode.getType();
        final Expression fieldExpr = propX(varX("this"), name);
        Expression initExpr = fNode.getInitialValueExpression();
        Statement assignInit;
        if (initExpr == null || (initExpr instanceof ConstantExpression && ((ConstantExpression)initExpr).isNullExpression())) {
            if (ClassHelper.isPrimitiveType(fType)) {
                assignInit = EmptyStatement.INSTANCE;
            } else {
                assignInit = assignS(fieldExpr, ConstantExpression.EMPTY_EXPRESSION);
            }
        } else {
            assignInit = assignS(fieldExpr, initExpr);
        }
        fNode.setInitialValueExpression(null);
        Expression value = findArg(name);
        return ifElseS(equalsNullX(value), assignInit, assignS(fieldExpr, castX(fType, value)));
    }

    public static ConstructorCallExpression ctorX(ClassNode type, Expression args) {
        return new ConstructorCallExpression(type, args);
    }

    public static ConstructorCallExpression ctorX(ClassNode type) {
        return new ConstructorCallExpression(type, ArgumentListExpression.EMPTY_ARGUMENTS);
    }

    public static Statement ctorSuperS(Expression args) {
        return stmt(ctorX(ClassNode.SUPER, args));
    }

    public static Statement ctorThisS(Expression args) {
        return stmt(ctorX(ClassNode.THIS, args));
    }

    public static Statement ctorSuperS() {
        return stmt(ctorX(ClassNode.SUPER));
    }

    public static Statement ctorThisS() {
        return stmt(ctorX(ClassNode.THIS));
    }

    public static Statement declS(Expression target, Expression init) {
        return new ExpressionStatement(new DeclarationExpression(target, ASSIGN, init));
    }

    public static BinaryExpression eqX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, EQ, rhv);
    }

    public static BooleanExpression equalsNullX(Expression argExpr) {
        return new BooleanExpression(eqX(argExpr, new ConstantExpression(null)));
    }

    public static FieldExpression fieldX(FieldNode fieldNode) {
        return new FieldExpression(fieldNode);
    }

    public static FieldExpression fieldX(ClassNode owner, String fieldName) {
        return new FieldExpression(owner.getField(fieldName));
    }

    public static Expression findArg(String argName) {
        return new PropertyExpression(new VariableExpression("args"), argName);
    }

    public static List<MethodNode> getAllMethods(ClassNode type) {
        ClassNode node = type;
        List<MethodNode> result = new ArrayList<MethodNode>();
        while (node != null) {
            result.addAll(node.getMethods());
            node = node.getSuperClass();
        }
        return result;
    }

    public static List<PropertyNode> getAllProperties(ClassNode type) {
        ClassNode node = type;
        List<PropertyNode> result = new ArrayList<PropertyNode>();
        while (node != null) {
            result.addAll(node.getProperties());
            node = node.getSuperClass();
        }
        return result;
    }

    public static String getGetterName(PropertyNode pNode) {
        return "get" + Verifier.capitalize(pNode.getName());
    }

    public static List<FieldNode> getInstanceNonPropertyFields(ClassNode cNode) {
        final List<FieldNode> result = new ArrayList<FieldNode>();
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static List<PropertyNode> getInstanceProperties(ClassNode cNode) {
        final List<PropertyNode> result = new ArrayList<PropertyNode>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode);
            }
        }
        return result;
    }

    public static List<FieldNode> getInstancePropertyFields(ClassNode cNode) {
        final List<FieldNode> result = new ArrayList<FieldNode>();
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static Set<ClassNode> getInterfacesAndSuperInterfaces(ClassNode type) {
        Set<ClassNode> res = new HashSet<ClassNode>();
        if (type.isInterface()) {
            res.add(type);
            return res;
        }
        ClassNode next = type;
        while (next != null) {
            Collections.addAll(res, next.getInterfaces());
            next = next.getSuperClass();
        }
        return res;
    }

    public static List<FieldNode> getSuperNonPropertyFields(ClassNode cNode) {
        final List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<FieldNode>();
        } else {
            result = getSuperNonPropertyFields(cNode.getSuperClass());
        }
        for (FieldNode fNode : cNode.getFields()) {
            if (!fNode.isStatic() && cNode.getProperty(fNode.getName()) == null) {
                result.add(fNode);
            }
        }
        return result;
    }

    public static List<FieldNode> getSuperPropertyFields(ClassNode cNode) {
        final List<FieldNode> result;
        if (cNode == ClassHelper.OBJECT_TYPE) {
            result = new ArrayList<FieldNode>();
        } else {
            result = getSuperPropertyFields(cNode.getSuperClass());
        }
        for (PropertyNode pNode : cNode.getProperties()) {
            if (!pNode.isStatic()) {
                result.add(pNode.getField());
            }
        }
        return result;
    }

    public static BinaryExpression hasClassX(Expression instance, ClassNode cNode) {
        return eqX(classX(cNode), callX(instance, "getClass"));
    }

    private static boolean hasClosureMember(AnnotationNode annotation) {

        Map<String, Expression> members = annotation.getMembers();
        for (Map.Entry<String, Expression> member : members.entrySet())  {
            if (member.getValue() instanceof ClosureExpression) return true;

            if (member.getValue() instanceof ClassExpression)  {
                ClassExpression classExpression = (ClassExpression) member.getValue();
                Class<?> typeClass = classExpression.getType().isResolved() ? classExpression.getType().redirect().getTypeClass() : null;
                if (typeClass != null && GeneratedClosure.class.isAssignableFrom(typeClass)) return true;
            }
        }

        return false;
    }

    public static boolean hasDeclaredMethod(ClassNode cNode, String name, int argsCount) {
        List<MethodNode> ms = cNode.getDeclaredMethods(name);
        for (MethodNode m : ms) {
            Parameter[] paras = m.getParameters();
            if (paras != null && paras.length == argsCount) {
                return true;
            }
        }
        return false;
    }

    public static BinaryExpression hasEqualFieldX(FieldNode fNode, Expression other) {
        return eqX(varX(fNode), propX(other, fNode.getName()));
    }

    public static BinaryExpression hasEqualPropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        return eqX(callThisX(getterName), callX(other, getterName));
    }

    public static BooleanExpression hasSameFieldX(FieldNode fNode, Expression other) {
        return sameX(varX(fNode), propX(other, fNode.getName()));
    }

    public static BooleanExpression hasSamePropertyX(PropertyNode pNode, Expression other) {
        String getterName = getGetterName(pNode);
        return sameX(callThisX(getterName), callX(other, getterName));
    }

    public static Statement ifElseS(Expression cond, Statement thenStmt, Statement elseStmt) {
        return new IfStatement(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : new BooleanExpression(cond),
                thenStmt,
                elseStmt
        );
    }

    public static Statement ifS(Expression cond, Expression trueExpr) {
        return ifS(cond, new ExpressionStatement(trueExpr));
    }

    public static Statement ifS(Expression cond, Statement trueStmt) {
        return new IfStatement(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : new BooleanExpression(cond),
                trueStmt,
                EmptyStatement.INSTANCE
        );
    }

    public static Expression indexX(Expression target, Expression value) {
        return new BinaryExpression(target, INDEX, value);
    }

    public static BooleanExpression isInstanceOfX(Expression objectExpression, ClassNode cNode) {
        return new BooleanExpression(new BinaryExpression(objectExpression, INSTANCEOF, classX(cNode)));
    }

    public static BooleanExpression isOneX(Expression expr) {
        return new BooleanExpression(new BinaryExpression(expr, EQ, new ConstantExpression(1)));
    }

    public static boolean isOrImplements(ClassNode type, ClassNode interfaceType) {
        return type.equals(interfaceType) || type.implementsInterface(interfaceType);
    }

    public static BooleanExpression isTrueX(Expression argExpr) {
        return new BooleanExpression(new BinaryExpression(argExpr, EQ, new ConstantExpression(Boolean.TRUE)));
    }

    public static BooleanExpression isZeroX(Expression expr) {
        return new BooleanExpression(new BinaryExpression(expr, EQ, new ConstantExpression(0)));
    }

    public static BinaryExpression ltX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, LT, rhv);
    }

    public static String makeDescriptorWithoutReturnType(MethodNode mn) {
        StringBuilder sb = new StringBuilder();
        sb.append(mn.getName()).append(':');
        for (Parameter p : mn.getParameters()) {
            sb.append(p.getType()).append(',');
        }
        return sb.toString();
    }

    public static BinaryExpression neX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, NE, rhv);
    }

    public static BooleanExpression notNullX(Expression argExpr) {
        return new BooleanExpression(new BinaryExpression(argExpr, NE, new ConstantExpression(null)));
    }

    public static NotExpression notX(Expression expr) {
        return new NotExpression(expr instanceof BooleanExpression ? expr : new BooleanExpression(expr));
    }

    public static BinaryExpression orX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, OR, rhv);
    }

    public static Parameter param(ClassNode type, String name) {
        return param(type, name, null);
    }

    public static Parameter param(ClassNode type, String name, Expression initialExpression) {
        Parameter param = new Parameter(type, name);
        if (initialExpression != null) {
            param.setInitialExpression(initialExpression);
        }
        return param;
    }

    public static Parameter[] params(Parameter... params) {
        return params != null ? params : Parameter.EMPTY_ARRAY;
    }

    public static BinaryExpression plusX(Expression lhv, Expression rhv) {
        return new BinaryExpression(lhv, PLUS, rhv);
    }

    public static Expression propX(Expression owner, String property) {
        return new PropertyExpression(owner, property);
    }

    public static Expression propX(Expression owner, Expression property) {
        return new PropertyExpression(owner, property);
    }

    public static Statement returnS(Expression expr) {
        return new ReturnStatement(new ExpressionStatement(expr));
    }

    public static Statement safeExpression(Expression fieldExpr, Expression expression) {
        return new IfStatement(
                equalsNullX(fieldExpr),
                new ExpressionStatement(fieldExpr),
                new ExpressionStatement(expression));
    }

    public static BooleanExpression sameX(Expression self, Expression other) {
        return new BooleanExpression(callX(self, "is", args(other)));
    }

    public static Statement stmt(Expression expr) {
        return new ExpressionStatement(expr);
    }

    public static TernaryExpression ternaryX(Expression cond, Expression trueExpr, Expression elseExpr) {
        return new TernaryExpression(
                cond instanceof BooleanExpression ? (BooleanExpression) cond : new BooleanExpression(cond),
                trueExpr,
                elseExpr);
    }

    public static VariableExpression varX(String name) {
        return new VariableExpression(name);
    }

    public static VariableExpression varX(Variable variable) {
        return new VariableExpression(variable);
    }

    public static VariableExpression varX(String name, ClassNode type) {
        return new VariableExpression(name, type);
    }
}
