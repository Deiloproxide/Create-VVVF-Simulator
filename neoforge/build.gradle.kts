import net.neoforged.moddevgradle.dsl.NeoForgeExtension
plugins{
    id("net.neoforged.moddev") version("2.0.141")
}
val platform:String=name
val mod_id:String=rootProject.property("mod_id").toString()
val minecraft_version:String=extra["minecraft_version"] as String
val shade:Configuration=extra["shade"] as Configuration
val deps:Map<String,String> =extra["deps"] as Map<String, String>
repositories{
    maven("https://maven.neoforged.net/releases")
}
configure<NeoForgeExtension>{
    version=deps["platform"]!!
    runs{
        create("client"){
            client()
            systemProperty("neoforge.enabledGameTestNamespaces",mod_id)
        }
        create("server"){
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces",mod_id)
        }
    }
    mods{
        create(mod_id){
            sourceSet(sourceSets.main.get())
        }
    }
}
configurations.named("additionalRuntimeClasspath"){
    extendsFrom(shade)
}
dependencies{
    implementation("com.simibubi.create:create-${minecraft_version}:${deps["create"]}:slim"){
        isTransitive=false
    }
    implementation("net.createmod.ponder:ponder-${platform}:${deps["ponder"]}+mc${minecraft_version}")
    compileOnly("dev.engine-room.flywheel:flywheel-${platform}-api-${minecraft_version}:${deps["flywheel"]}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-${platform}-${minecraft_version}:${deps["flywheel"]}")
    implementation("com.tterrag.registrate:Registrate:${deps["registrate"]}")
}