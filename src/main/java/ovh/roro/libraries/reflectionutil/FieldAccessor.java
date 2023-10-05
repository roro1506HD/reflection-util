package ovh.roro.libraries.reflectionutil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldAccessor {

    private final @Nullable Class<?> clazz;
    private final @NotNull Unsafe unsafe;
    private final boolean isStatic;
    private final long fieldOffset;

    FieldAccessor(@Nullable Class<?> clazz, @NotNull Field field, @NotNull Unsafe unsafe) {
        this.clazz = clazz;
        this.unsafe = unsafe;
        this.isStatic = Modifier.isStatic(field.getModifiers());
        this.fieldOffset = this.isStatic ? unsafe.staticFieldOffset(field) : unsafe.objectFieldOffset(field);
    }

    FieldAccessor(@Nullable Class<?> clazz, @NotNull Unsafe unsafe, boolean isStatic, long fieldOffset) {
        this.clazz = clazz;
        this.unsafe = unsafe;
        this.isStatic = isStatic;
        this.fieldOffset = fieldOffset;
    }

    // SETTERS

    public void setObject(@Nullable Object instance, @Nullable Object value) {
        if (this.isStatic) {
            this.unsafe.putObject(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putObject(instance, this.fieldOffset, value);
        }
    }

    public void setByte(@Nullable Object instance, byte value) {
        if (this.isStatic) {
            this.unsafe.putByte(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putByte(instance, this.fieldOffset, value);
        }
    }

    public void setShort(@Nullable Object instance, short value) {
        if (this.isStatic) {
            this.unsafe.putShort(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putShort(instance, this.fieldOffset, value);
        }
    }

    public void setInt(@Nullable Object instance, int value) {
        if (this.isStatic) {
            this.unsafe.putInt(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putInt(instance, this.fieldOffset, value);
        }
    }

    public void setLong(@Nullable Object instance, long value) {
        if (this.isStatic) {
            this.unsafe.putLong(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putLong(instance, this.fieldOffset, value);
        }
    }

    public void setFloat(@Nullable Object instance, float value) {
        if (this.isStatic) {
            this.unsafe.putFloat(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putFloat(instance, this.fieldOffset, value);
        }
    }

    public void setDouble(@Nullable Object instance, double value) {
        if (this.isStatic) {
            this.unsafe.putDouble(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putDouble(instance, this.fieldOffset, value);
        }
    }

    public void setChar(@Nullable Object instance, char value) {
        if (this.isStatic) {
            this.unsafe.putChar(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putChar(instance, this.fieldOffset, value);
        }
    }

    public void setBoolean(@Nullable Object instance, boolean value) {
        if (this.isStatic) {
            this.unsafe.putBoolean(this.clazz, this.fieldOffset, value);
        } else {
            this.unsafe.putBoolean(instance, this.fieldOffset, value);
        }
    }

    // GETTERS

    @SuppressWarnings("unchecked")
    public <T> T getObject(@Nullable Object instance) {
        if (this.isStatic) {
            return (T) this.unsafe.getObject(this.clazz, this.fieldOffset);
        }

        return (T) this.unsafe.getObject(instance, this.fieldOffset);
    }

    public byte getByte(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getByte(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getByte(instance, this.fieldOffset);
    }

    public short getShort(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getShort(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getShort(instance, this.fieldOffset);
    }

    public int getInt(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getInt(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getInt(instance, this.fieldOffset);
    }

    public long getLong(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getLong(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getLong(instance, this.fieldOffset);
    }

    public float getFloat(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getFloat(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getFloat(instance, this.fieldOffset);
    }

    public double getDouble(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getDouble(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getDouble(instance, this.fieldOffset);
    }

    public char getChar(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getChar(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getChar(instance, this.fieldOffset);
    }

    public boolean getBoolean(@Nullable Object instance) {
        if (this.isStatic) {
            return this.unsafe.getBoolean(this.clazz, this.fieldOffset);
        }

        return this.unsafe.getBoolean(instance, this.fieldOffset);
    }
}
