import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class fromBinaryStringToByte {
	static String[] InstructionMemory; // each instruction is 16 bits
	static Byte[] DataMemory; // each block is 8 bits
	static Byte[] GPRegisterFile; // each register is 8 bits
	static Byte statusRegister; // 8 bits with only 5 flags 000CVNSZ
	static Short pc; // 16 bits
	static int NumberOfInstructions;
	static int TotalCycles;
	static String fetchedInstruction;
	static String opcode;
	static String parameter1;
	static String parameter2;
	static int delayCount;
	static boolean end;

	public fromBinaryStringToByte() {
		InstructionMemory = new String[1024];
		DataMemory = new Byte[2048];
		GPRegisterFile = new Byte[64];
		pc = 0;
		end = false;
	}

	public static void readAssembly() throws IOException {
		File file = new File("src\\assemblyCode.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		String instr;
		while ((instr = br.readLine()) != null) {
			String[] field = instr.split(" ");
			String binary = "";
			// getting opcode in binary
			switch (field[0].toLowerCase()) {
			case "add":
				binary += "0000";
				break;
			case "sub":
				binary += "0001";
				break;
			case "mul":
				binary += "0010";
				break;
			case "movi":
				binary += "0011";
				break;
			case "beqz":
				binary += "0100";
				break;
			case "andi":
				binary += "0101";
				break;
			case "eor":
				binary += "0110";
				break;
			case "br":
				binary += "0111";
				break;
			case "sal":
				binary += "1000";
				break;
			case "sar":
				binary += "1001";
				break;
			case "ldr":
				binary += "1010";
				break;
			case "str":
				binary += "1011";
				break;
			default:
				System.out.println("Undefined Instruction");

			}
			String firstReg = field[1].substring(1); // get Regnumber without R
			int firstRegNum = Integer.parseInt(firstReg);
			String firstRegBinary = Integer.toBinaryString(firstRegNum);
			for (int i = firstRegBinary.length(); i < 6; i++) {
				firstRegBinary = "0" + firstRegBinary;
			}
			firstRegBinary = firstRegBinary.substring(firstRegBinary.length()-6, firstRegBinary.length());
			
			
			String secondParameterBinary;
			if (field[2].toLowerCase().charAt(0) == 'r') {
				// R-type
				String secReg = field[2].substring(1); // get Regnumber without R
				int secRegNum = Integer.parseInt(secReg);
				secondParameterBinary = Integer.toBinaryString(secRegNum);
			} else {
				// I-type = immediate
				int imm = Integer.parseInt(field[2]);
				secondParameterBinary = Integer.toBinaryString(imm);
			}
			for (int i = secondParameterBinary.length(); i < 6; i++) {
				secondParameterBinary = "0" + secondParameterBinary;
			}
			secondParameterBinary = secondParameterBinary.substring(secondParameterBinary.length()-6, secondParameterBinary.length());
			
			binary += firstRegBinary + secondParameterBinary;
			InstructionMemory[NumberOfInstructions] = binary;
			NumberOfInstructions++;
		}
		// 3 + ((n - 1) * 1)
		TotalCycles = 3 + ((NumberOfInstructions - 1) * 1);
		br.close();
	}

	public static void fetch() {
		fetchedInstruction = InstructionMemory[pc];
		if (fetchedInstruction == null)
		{
			end = true;
			return;
		}
		System.out.println("___________IF______________");
		pc++;
		System.out.println("Fetched Instruction: " + fetchedInstruction);
		System.out.println("PC new value: " + pc);
	}

	public static void decode(String instruction) {
		System.out.println("___________ID______________");
		opcode = instruction.substring(0, 4);
		parameter1 = instruction.substring(4, 10);
		parameter2 = instruction.substring(10, 16);
		System.out.println("Decoded Instruction: ");
		System.out.println("Opcode: " + opcode + " ,Register: " + parameter1 + " ,Parameter2: " + parameter2);

	}

	public static void execute() {
		System.out.println("___________EX______________");
		System.out.println("Instruction being executed: " + opcode + parameter1 + parameter2);
		// register value from register file
		Byte firstReg = GPRegisterFile[Integer.parseInt(parameter1, 2)];
		Byte secondReg = GPRegisterFile[Integer.parseInt(parameter2, 2)];
		// 2nd parameter is an immediate
		Byte immediate = parseBinaryStringToByte(parameter2);

		// Temporary variables
		int temp1;
		int temp2;
		String temp1Str;
		String temp2Str;
		int bit9, bit8;
		int V, C, N, S, Z;
		int[] carries;
		int result;
		Byte finalResult;
		String allFlags;

		switch (opcode) {
		case "0000": // R-type- ADD
			System.out.println("=======ADD=======");
			// get result in bytes
			finalResult = (byte) (firstReg + secondReg);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// preparing the status register values
			temp1 = firstReg & 0b011111111;
			temp2 = secondReg & 0b011111111;
			// convert the 9 bits to binary string
			temp1Str = Integer.toBinaryString(temp1);
			temp2Str = Integer.toBinaryString(temp2);
			// getting carry bit C
			bit9 = (temp1 + temp2) & 0b100000000; // 9th bit
			C = 0; // bit4 of statusReg
			if (bit9 == 0b100000000)
				C = 1;
			else
				C = 0;
			// getting overflow bit V
			V = 0;
			if (firstReg < 0 && secondReg < 0 && finalResult >= 0)
				V = 1;
			else if (firstReg > 0 && secondReg > 0 && finalResult <= 0)
				V = 1;

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;

			// getting Sign bit S
			S = N ^ V;

			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "000" + C + V + N + S + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;

		case "0001": // R-type - SUB
			System.out.println("=======SUB=======");
			// get result in bytes
			finalResult = (byte) (firstReg - secondReg);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// preparing the status register values
			// getting overflow bit V
			V = 0;
			if (firstReg < 0 && secondReg > 0 && finalResult > 0)
				V = 1;
			else if (firstReg > 0 && secondReg < 0 && finalResult < 0)
				V = 1;

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;

			// getting Sign bit S
			S = N ^ V;

			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "0000" + V + N + S + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "0010": // R-type - MUL
			System.out.println("=======MUL=======");
			// get result in bytes
			finalResult = (byte) (firstReg * secondReg);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;
			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "00000" + N + "0" + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "0110": // R-type - EOR
			System.out.println("=======EOR=======");
			// get result in bytes
			finalResult = (byte) (firstReg ^ secondReg);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;
			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "00000" + N + "0" + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "0111": // R-type - BR
			System.out.println("=======BR=======");
			temp1Str = Integer.toBinaryString(firstReg);
			temp2Str = Integer.toBinaryString(secondReg);
			String newAddress = temp1Str + temp2Str;
			pc = Short.parseShort(newAddress);
			System.out.println("PC new value: " + pc);
			// handling control hazard
			opcode = null;
			parameter1 = null;
			parameter2 = null;
			delayCount = 2;
			break;
		case "0011": // I-type - MOVI
			System.out.println("=======MOVI=======");
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = immediate;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + immediate);
			break;
		case "0100": // I-type - BEQZ
			System.out.println("=======BEQZ=======");
			if (firstReg == 0) {
				pc = (short) (pc + immediate);
				System.out.println("PC new value: " + pc);
			}
			break;
		case "0101": // I-type - ANDI
			System.out.println("=======ANDI=======");
			finalResult = (byte) (firstReg & immediate);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;
			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "00000" + N + "0" + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "1000": // I-type - SAL
			System.out.println("=======SAL=======");
			finalResult = (byte) (firstReg << immediate);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);

			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;
			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "00000" + N + "0" + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "1001": // I-type - SAR
			System.out.println("=======SAR=======");
			finalResult = (byte) (firstReg >> immediate);
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = finalResult;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + finalResult);
			// getting Negative bit N
			N = 0;
			bit8 = finalResult & 0b10000000;
			if (bit8 == 0b10000000)
				N = 1;
			else
				N = 0;
			// getting Zero bit Z
			Z = 0;
			if (finalResult == 0)
				Z = 1;
			else
				Z = 0;
			allFlags = "00000" + N + "0" + Z;
			statusRegister = Byte.parseByte(allFlags, 2);
			System.out.println("Status Register new value: " + allFlags);
			break;
		case "1010": // I-type - LDR
			System.out.println("=======LDR=======");
			Byte data = DataMemory[immediate];
			GPRegisterFile[Integer.parseInt(parameter1, 2)] = data;
			System.out.println("Register[" + Integer.parseInt(parameter1, 2) + "] new value: " + data);
			break;
		case "1011": // I-type - STR
			System.out.println("=======STR=======");
			DataMemory[immediate] = firstReg;
			System.out.println("DataMemory[" + immediate + "] new value: " + firstReg);
			break;
		default:
			break;
		}
	}

	public static Byte parseBinaryStringToByte(String binaryString) {
	    int length = binaryString.length();
	    boolean isNegative = binaryString.charAt(0) == '1';
	    int intValue = 0;
	    for (int i = 1; i < length; i++) {
	        int bitValue = binaryString.charAt(i) - '0';
	        intValue = (intValue << 1) | bitValue;
	    }
	    // Apply 2's complement if the value is negative
	    if (isNegative) {
	        intValue = negate(intValue, length - 1);
	        intValue = -(intValue+1);
	    }
	    byte byteValue = (byte) intValue;

	    return byteValue;
	}

	private static int negate(int value, int numBits) {
	    int mask = (1 << numBits) - 1;
	    return value ^ mask;
	}
	public static void main(String[] args) throws IOException {
		fromBinaryStringToByte hsa = new fromBinaryStringToByte();
		readAssembly();
		for (int i = 1; i <= TotalCycles; i++) {
			System.out.println("-------------------------------");
			System.out.println("Clock Cycle number: " + i);
			System.out.println("-------------------------------");

			if (i != 1 && i != 2) {
				if (delayCount == 0) {
					execute();
				} else {
					delayCount--;
				}
			}
			if (i != 1 && !end) {
				if (delayCount == 0 || delayCount == 1) {
					decode(fetchedInstruction);
				} else {
					delayCount--;
				}
			}
			if (!end)
				fetch();

			System.out.println();
			System.out.println();
		}
		System.out.println("----------------Content of all Registers------------------");
		for (int i = 0; i < GPRegisterFile.length; i++) {
			System.out.println("Register " + i + ": " + GPRegisterFile[i]);
		}
		System.out.println("PC: " + pc);
		System.out.println("Status Register: " + statusRegister);
		System.out.println();

		System.out.println("----------------Content of Instuction Memory------------------");
		for (int i = 0; i < InstructionMemory.length; i++) {
			System.out.println("Instruction " + i + ": " + InstructionMemory[i]);
		}
		System.out.println();

		System.out.println("----------------Content of Data Memory------------------");
		for (int i = 0; i < DataMemory.length; i++) {
			System.out.println("Block " + i + ": " + DataMemory[i]);
		}

	}
}