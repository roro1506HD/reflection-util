package ovh.roro.libraries.reflectionutil;

import com.google.common.collect.ImmutableMap;
import io.papermc.paper.util.MappingEnvironment;
import io.papermc.paper.util.StringPool;
import net.neoforged.srgutils.IMappingFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObfuscationHelper {

    private static final @NotNull Logger LOGGER = Logger.getLogger("ObfuscationHelper");

    private static final @NotNull Map<Class<?>, String> KNOWN_SIGNATURES = Map.of(
            Boolean.TYPE, "Z",
            Byte.TYPE, "B",
            Character.TYPE, "C",
            Short.TYPE, "S",
            Integer.TYPE, "I",
            Long.TYPE, "J",
            Float.TYPE, "F",
            Double.TYPE, "D",
            Void.TYPE, "V"
    );

    private final boolean isInMojangMappedEnvironment;

    private final @Nullable Map<String, ClassMapping> classMappingsByObfName;
    private final @Nullable Map<String, ClassMapping> classMappingsByMojangName;

    ObfuscationHelper() {
        this.isInMojangMappedEnvironment = this.checkForMojangEnvironment();

        Set<ClassMapping> mappings = this.loadMappingsIfPresent();

        if (mappings == null) {
            this.classMappingsByObfName = null;
            this.classMappingsByMojangName = null;
        } else {
            this.classMappingsByObfName = this.toMap(mappings, ClassMapping::obfName);
            this.classMappingsByMojangName = this.toMap(mappings, ClassMapping::mojangName);
        }
    }

    private boolean checkForMojangEnvironment() {
        try {
            Class.forName("net.minecraft.network.FriendlyByteBuf");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private @Nullable ClassMapping getClassMapping(@NotNull Class<?> clazz) {
        String name = this.getCanonicalName(clazz);

        if (name == null) {
            return null;
        }

        if (this.isInMojangMappedEnvironment) {
            ClassMapping mapping = this.getClassMapping(name, this.classMappingsByMojangName, Function.identity());

            if (mapping != null) {
                return mapping;
            }
        }

        return this.getClassMapping(name, this.classMappingsByObfName, Function.identity());
    }

    private <T> @Nullable T getClassMapping(@NotNull String name, @Nullable Map<String, ClassMapping> mappings, @NotNull Function<ClassMapping, T> mapper) {
        if (mappings == null) {
            return null;
        }

        ClassMapping classMapping = mappings.get(name);

        if (classMapping == null) {
            return null;
        }

        return mapper.apply(classMapping);
    }

    private @NotNull String getMemberMapping(@NotNull Class<?> clazz, @NotNull Function<ClassMapping, Map<String, Mapping>> mappingType, @NotNull String searchName, @NotNull String returnName, @NotNull Function<Mapping, String> mapper) {
        ClassMapping classMapping = this.getClassMapping(clazz);

        if (classMapping == null) {
            return returnName;
        }

        Mapping mapping = mappingType.apply(classMapping).get(searchName);

        if (mapping == null) {
            return returnName;
        }

        return mapper.apply(mapping);
    }

    public @NotNull String getObfFieldName(@NotNull Class<?> clazz, @NotNull String mojangName) {
        return this.getMemberMapping(clazz, ClassMapping::fieldMappingsByMojangName, mojangName, mojangName, Mapping::obfName);
    }

    public @NotNull String getMojangFieldName(@NotNull Class<?> clazz, @NotNull String obfName) {
        return this.getMemberMapping(clazz, ClassMapping::fieldMappingsByObfName, obfName, obfName, Mapping::mojangName);
    }

    public @NotNull String getObfMethodName(@NotNull Class<?> clazz, @NotNull String mojangName, @NotNull Class<?> @NotNull [] params, @NotNull Class<?> returnType) {
        return this.getMemberMapping(clazz, ClassMapping::methodMappingsByMojangName, mojangName + this.getMethodSignature(params, returnType, this::getMojangClassName), mojangName, Mapping::obfName);
    }

    public @NotNull String getMojangMethodName(@NotNull Class<?> clazz, @NotNull String obfName, @NotNull Class<?> @NotNull [] params, @NotNull Class<?> returnType) {
        return this.getMemberMapping(clazz, ClassMapping::methodMappingsByObfName, obfName + this.getMethodSignature(params, returnType, this::getObfClassName), obfName, Mapping::obfName);
    }

    public @Nullable String getObfClassName(@NotNull String mojangName) {
        return this.getClassMapping(mojangName, this.classMappingsByMojangName, ClassMapping::obfName);
    }

    public @Nullable String getMojangClassName(@NotNull String obfName) {
        return this.getClassMapping(obfName, this.classMappingsByObfName, ClassMapping::mojangName);
    }

    private @Nullable String getCanonicalName(@NotNull Class<?> clazz) {
        if (clazz.isArray()) {
            String canonicalName = this.getCanonicalName(clazz.getComponentType());

            if (canonicalName != null) {
                return canonicalName + "[]";
            }
            return null;
        }

        if (clazz.isHidden() || clazz.isLocalClass() || clazz.isAnonymousClass()) {
            return null;
        }

        Class<?> enclosingClass = clazz.getEnclosingClass();

        if (enclosingClass == null) {
            return clazz.getName();
        }

        StringBuilder builder = new StringBuilder(clazz.getSimpleName());

        while (enclosingClass != null) {
            builder
                    .insert(0, '$')
                    .insert(0, this.getCanonicalName(enclosingClass));

            enclosingClass = enclosingClass.getEnclosingClass();
        }

        return builder.toString();
    }

    private @NotNull String getMethodSignature(@NotNull Class<?> @NotNull [] args, @NotNull Class<?> returnType, @NotNull Function<String, String> classNameMapper) {
        StringBuilder builder = new StringBuilder("(");

        for (Class<?> arg : args) {
            builder.append(this.getSignature(arg, classNameMapper));
        }

        return builder.append(')').append(this.getSignature(returnType, classNameMapper)).toString();
    }

    private @NotNull String getSignature(@NotNull Class<?> clazz, @NotNull Function<String, String> classNameMapper) {
        String knownSignature = ObfuscationHelper.KNOWN_SIGNATURES.get(clazz);

        if (knownSignature != null) {
            return knownSignature;
        }

        if (clazz.isArray()) {
            return "[" + this.getSignature(clazz.componentType(), classNameMapper);
        }

        String clazzName = clazz.getName();
        String mappedName = classNameMapper.apply(clazzName);

        if (mappedName != null) {
            clazzName = mappedName;
        }

        return "L" + clazzName.replace('.', '/') + ";";
    }

    private @Nullable Set<ClassMapping> loadMappingsIfPresent() {
        if (!MappingEnvironment.hasMappings()) {
            ObfuscationHelper.LOGGER.severe(() -> "Failed to load mappings for reflection utils: mappings not found");
            return null;
        }

        try (InputStream mappingsInputStream = MappingEnvironment.mappingsStream()) {
            IMappingFile mappings = IMappingFile.load(mappingsInputStream); // Mappings are mojang->spigot
            Set<ClassMapping> classes = new HashSet<>();
            StringPool pool = new StringPool();

            for (IMappingFile.IClass cls : mappings.getClasses()) {
                Set<Mapping> methods = new HashSet<>();
                Set<Mapping> fields = new HashSet<>();

                for (IMappingFile.IMethod methodMapping : cls.getMethods()) {
                    methods.add(new Mapping(
                            pool.string(Objects.requireNonNull(methodMapping.getMapped())),
                            pool.string(Objects.requireNonNull(methodMapping.getOriginal())),
                            pool.string(Objects.requireNonNull(methodMapping.getMappedDescriptor())),
                            pool.string(Objects.requireNonNull(methodMapping.getDescriptor()))
                    ));
                }

                for (IMappingFile.IField field : cls.getFields()) {
                    fields.add(new Mapping(
                            Objects.requireNonNull(field.getMapped()),
                            Objects.requireNonNull(field.getOriginal()),
                            null,
                            null
                    ));
                }

                classes.add(new ClassMapping(
                        Objects.requireNonNull(cls.getMapped()).replace('/', '.'),
                        Objects.requireNonNull(cls.getOriginal()).replace('/', '.'),

                        this.toMap(methods, mapping -> mapping.obfName() + mapping.obfDescription()),
                        this.toMap(methods, mapping -> mapping.mojangName() + mapping.mojangDescription()),

                        this.toMap(fields, Mapping::obfName),
                        this.toMap(fields, Mapping::mojangName)
                ));
            }

            return Set.copyOf(classes);
        } catch (final IOException ex) {
            ObfuscationHelper.LOGGER.log(Level.SEVERE, "Failed to load mappings for reflection utils:", ex);
            return null;
        }
    }

    private <T> @NotNull Map<String, T> toMap(@NotNull Set<T> mappings, @NotNull Function<T, String> keyMapper) {
        ImmutableMap.Builder<String, T> builder = ImmutableMap.builder();

        for (T mapping : mappings) {
            builder.put(keyMapper.apply(mapping), mapping);
        }

        return builder.build();
    }

    public record Mapping(
            @NotNull String obfName,
            @NotNull String mojangName,
            @Nullable String obfDescription,
            @Nullable String mojangDescription
    ) {
    }

    public record ClassMapping(
            @NotNull String obfName,
            @NotNull String mojangName,

            @NotNull Map<String, Mapping> methodMappingsByObfName,
            @NotNull Map<String, Mapping> methodMappingsByMojangName,

            @NotNull Map<String, Mapping> fieldMappingsByObfName,
            @NotNull Map<String, Mapping> fieldMappingsByMojangName
    ) {
    }
}
