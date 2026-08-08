import net.minecraftforge.gradle.common.util.MinecraftExtension
import net.minecraftforge.gradle.userdev.DependencyManagementExtension
import net.minecraftforge.gradle.userdev.tasks.RenameJarInPlace
import org.spongepowered.asm.gradle.plugins.MixinExtension
buildscript{
    repositories{
        maven("https://repo.spongepowered.org/repository/maven-public/")
    }
    dependencies{
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}
plugins{
    id("net.minecraftforge.gradle") version("6.0.54")
}
apply(plugin="org.spongepowered.mixin")
val platform:String=name
val mod_id:String=rootProject.property("mod_id").toString()
val minecraft_version:String=extra["minecraft_version"] as String
val shade:Configuration=extra["shade"] as Configuration
val deps:Map<String,String> =extra["deps"] as Map<String,String>
repositories{
    maven("https://maven.minecraftforge.net")
}
configure<MinecraftExtension>{
    mappings("official",minecraft_version)
    runs{
        create("client"){
            property("forge.enabledGameTestNamespaces",mod_id)
        }
        create("server"){
            args("--nogui")
            property("forge.enabledGameTestNamespaces",mod_id)
        }
        configureEach{
            mods.create(mod_id){
                source(sourceSets.main.get())
            }
        }
    }
}
configure<MixinExtension>{
    config("${mod_id}.mixins.json")
}
configurations.named("minecraftLibrary"){
    extendsFrom(shade)
}
fun fgDeobf(dependency:String):Dependency=extensions.getByType<DependencyManagementExtension>().deobf(dependency)
fun fgDeobf(dependency:String,configuration:Action<ExternalModuleDependency>):Dependency=
    fgDeobf(dependency).also{
        dep->(dep as? ExternalModuleDependency)?.let{configuration.execute(it)}
    }
dependencies{
    minecraft("net.minecraftforge:forge:${minecraft_version}-${deps["platform"]}")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    implementation(fgDeobf("com.simibubi.create:create-${minecraft_version}:${deps["create"]}:slim"){
        isTransitive=false
    })
    implementation(fgDeobf("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${deps["ponder"]}"))
    compileOnly(fgDeobf("dev.engine-room.flywheel:flywheel-${platform}-api-${minecraft_version}:${deps["flywheel"]}"))
    runtimeOnly(fgDeobf("dev.engine-room.flywheel:flywheel-${platform}-${minecraft_version}:${deps["flywheel"]}"))
    implementation(fgDeobf("com.tterrag.registrate:Registrate:${deps["registrate"]}"))
    implementation("io.github.llamalad7:mixinextras-forge:${deps["mixinextras"]}")
}
(extensions.getByName("reobf") as NamedDomainObjectContainer<RenameJarInPlace>).create("shadowJar")