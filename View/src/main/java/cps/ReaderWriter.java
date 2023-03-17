package cps;

import cps.signals.Signal;

import java.io.*;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cps.signals.SignalParamCalculator.round_values;

public class ReaderWriter {



    public static Signal readBinary(File file) {
                int i = 0;

                Duration duration = null, probingPeriod = null;
                float t1 = -1.0f;
                char type = ' ';
                List<Double> probes = new ArrayList<>();
                try {
                    FileInputStream fi = new FileInputStream(file);
                    DataInputStream dis = new DataInputStream(fi);
                    int count = fi.available();
                    byte[] bytes = new byte[count];
                    dis.read(bytes);
                    while (i < count) {
                        if (i == 0) {
                            t1 = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                        } else if (i == 4) {
                            probingPeriod = Duration.ofNanos(
                                    1000_000_000L / getByteBuffer(Arrays.copyOfRange(bytes, i, i += Integer.BYTES)).getInt());
                        } else if (i == 8) {
                            type = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Character.BYTES)).getChar();
                        } else {
                            float sample = getByteBuffer(Arrays.copyOfRange(bytes, i, i += Float.BYTES)).getFloat();
                            double probe = (double) sample;
                            probes.add(probe);
                        }
                    }
                    duration = Duration.ofNanos(probingPeriod.toNanos() * probes.size());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Signal.Type resultType;

                long t1inNanos = (long) t1 * 1000_000_000L;
                if (type == Signal.Type.CONTINUOUS.toString().charAt(0)) {
                    resultType = Signal.Type.CONTINUOUS;
                } else {
                    resultType = Signal.Type.DISCRETE;
                }

                return new Signal(resultType, duration, probingPeriod, probes);
    }

    public static void writeBinary(File file, float t1, long fq, Signal signal) {
        prepareToSave(signal);
        try (FileOutputStream fos = new FileOutputStream(file)) {

            DataOutputStream dos = new DataOutputStream(fos);
            dos.writeFloat(t1);
            dos.writeInt((int) fq);
            dos.writeChar(signal.getType().toString().charAt(0));
            for (double d : signal.getSamples()) {
                dos.writeFloat((float) d);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ByteBuffer getByteBuffer(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
        byteBuffer.put(bytes);
        byteBuffer.flip();
        return byteBuffer;
    }

    private static void prepareToSave(Signal signal) {
        for (int i = 0; i < signal.getSamples().size(); i++) {
            double rounded = signal.getSamples().get(i);
            rounded = round_values(rounded, 2);
            signal.getSamples().set(i, rounded);
        }
    }
}
