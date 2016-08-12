package de.jwi.ostendo.jacorbidl;

/*
 * Ostendo - CORBA IIOP Message Analyzer
 * 
 * Copyright (C) 2006 Juergen Weber
 * 
 * This file is part of Ostendo.
 * 
 * Ostendo is free software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * Ostendo is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with Ostendo; if not, write to the Free
 * Software Foundation, Inc., 59 Temple Place, Suite 330, Boston
 */


import org.jacorb.idl.AliasTypeSpec;
import org.jacorb.idl.ConstrTypeSpec;
import org.jacorb.idl.Declaration;
import org.jacorb.idl.Definition;
import org.jacorb.idl.Definitions;
import org.jacorb.idl.EnumType;
import org.jacorb.idl.Interface;
import org.jacorb.idl.InterfaceBody;
import org.jacorb.idl.Method;
import org.jacorb.idl.Module;
import org.jacorb.idl.NativeType;
import org.jacorb.idl.OpDecl;
import org.jacorb.idl.ParamDecl;
import org.jacorb.idl.SimpleTypeSpec;
import org.jacorb.idl.Spec;
import org.jacorb.idl.StructType;
import org.jacorb.idl.TypeDeclaration;
import org.jacorb.idl.TypeDef;
import org.jacorb.idl.UnionType;
import org.jacorb.idl.Value;
import org.jacorb.idl.VectorType;


/**
 * @author Juergen Weber 
 * Created on 01.08.2006
 */
public class IDLTreeVisitorImpl implements org.jacorb.idl.IDLTreeVisitor
{

	public void visitAlias(AliasTypeSpec alias)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitConstrTypeSpec(ConstrTypeSpec typeDecl)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitDeclaration(Declaration decl)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitDefinition(Definition def)
	{
		System.out.println("visitDefinition");
		
		
	}

	public void visitDefinitions(Definitions defs)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitEnum(EnumType enumType)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitInterface(Interface intf)
	{
		System.out.println("visitInterface");
		
	}

	public void visitInterfaceBody(InterfaceBody body)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitMethod(Method m)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitModule(Module mod)
	{
		System.out.println("visitModule");
		
	}

	public void visitNative(NativeType _native)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitOpDecl(OpDecl decl)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitParamDecl(ParamDecl param)
	{
		// TODO Auto-generated method stub
		
	}

	public void visitSpec(Spec spec)
	{
//		System.out.println("visitSpec");
		
		ParserCaller.getInstance().setParsedSpec(spec);
		
	}

	public void visitStruct(StructType struct)
	{
		System.out.println("");
		
	}

	public void visitTypeDeclaration(TypeDeclaration typeDecl)
	{
		System.out.println("");
		
	}

	public void visitTypeDef(TypeDef typedef)
	{
		System.out.println("");
		
	}

	public void visitUnion(UnionType union)
	{
		System.out.println("");
		
	}

	public void visitValue(Value value)
	{
		System.out.println("");
		
	}

	public void visitSimpleTypeSpec(SimpleTypeSpec arg0)
	{
		// TODO Auto-generated method stub
		if (true) throw new RuntimeException("visitSimpleTypeSpec not implemented");
		
	}

	public void visitVectorType(VectorType arg0)
	{
		// TODO Auto-generated method stub
		if (true) throw new RuntimeException("visitVectorType not implemented");
		
	}

}
