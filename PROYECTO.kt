import java.io.File
import java.util.*

data class Edge(val bus: String, val start: String, val destination: String, val fuelCost: Double)
data class Route(val start: String, val destination: String, val distance: Double)
data class BusRoute(val bus: String, val route: List<String>, val fuelCost: Double) // Nueva clase para la ruta completa

class Graph {
    private val edges = mutableListOf<Edge>()
    private val parent = mutableMapOf<String, String>()
    private val distances = mutableMapOf<String, Double>()
    private val busRoutes = mutableListOf<BusRoute>() // Lista de rutas completas de autobuses

    fun addRoute(start: String, destination: String, distance: Double) {
        distances["$start-$destination"] = distance
        distances["$destination-$start"] = distance
    }

    fun addEdge(bus: String, start: String, destination: String, fuelCost: Double) {
        edges.add(Edge(bus, start, destination, fuelCost))
    }

    fun getDistance(start: String, destination: String): Double? {
        return distances["$start-$destination"]
    }

    private fun find(node: String): String {
        if (parent[node] == node) return node
        val root = find(parent[node]!!)
        parent[node] = root
        return root
    }

    private fun union(node1: String, node2: String) {
        val root1 = find(node1)
        val root2 = find(node2)
        if (root1 != root2) {
            parent[root2] = root1
        }
    }

    fun kruskal(): List<Edge> {
        val mst = mutableListOf<Edge>()
        val nodes = edges.flatMap { listOf(it.start, it.destination) }.toSet()
        nodes.forEach { parent[it] = it }

        edges.sortBy { it.fuelCost }

        for (edge in edges) {
            if (find(edge.start) != find(edge.destination)) {
                mst.add(edge)
                union(edge.start, edge.destination)
            }
        }
        return mst
    }

    fun addBusRoute(bus: String, route: List<String>, fuelCost: Double) {
        busRoutes.add(BusRoute(bus, route, fuelCost))
    }

    fun getBusRoute(bus: String): BusRoute? { // Nuevo método para acceder a busRoutes
        return busRoutes.find { it.bus == bus }
    }
}

fun main() {
    // Nombre del archivo de entrada
    val fileName = "input.txt" // Puedes cambiar este nombre si lo deseas

    // Leer el archivo de entrada
    val scanner = Scanner(File(fileName))

    // Leer cantidad de autobuses y recorridos
    val numBuses = scanner.nextInt()
    val numRoutes = scanner.nextInt()
    scanner.nextLine() // Consumir línea vacía

    val graph = Graph()

    // Leer la ruta principal con múltiples destinos y distancias
    val routeInfo = scanner.nextLine().split(" ")
    for (i in 0 until routeInfo.size - 1 step 2) {
        val destination = routeInfo[i]
        val distance = routeInfo[i + 1].toDouble()
        graph.addRoute("USB", destination, distance)
    }

    // Leer recorridos de los autobuses con múltiples puntos
    repeat(numRoutes) {
        val line = scanner.nextLine().split(" ")
        val bus = line[0]    // Nombre del autobús (Bus1, Bus2, etc.)
        val fuelCost = line.last().toDouble() // Consumo de combustible
        val route = line.subList(1, line.size - 1) // Lista de puntos intermedios y destino

        graph.addBusRoute(bus, route, fuelCost) // Almacenar la ruta completa

        // Crear aristas para los puntos intermedios
        for (i in 0 until route.size - 1) {
            val start = if (i == 0) "USB" else route[i - 1]
            val destination = route[i]
            graph.addEdge(bus, start, destination, fuelCost)
        }
    }

    // Ejecutar Kruskal para obtener el MST
    val mst = graph.kruskal()

    // Encontrar la ruta más óptima (la de menor costo total de combustible)
    val rutaOptima = mst.minByOrNull { it.fuelCost }

    // Imprimir solo la ruta más óptima
    if (rutaOptima != null) {
        val busRoute = graph.getBusRoute(rutaOptima.bus) // Usar el nuevo método
        val distancia = graph.getDistance(rutaOptima.start, rutaOptima.destination) ?: "Desconocida"
        println("\nRuta más óptima:")
        print("${busRoute?.bus}: ")
        busRoute?.route?.forEach { print("$it ") } // Imprimir todos los puntos
        println("(${distancia} km, ${rutaOptima.fuelCost} L)")
    } else {
        println("\nNo se encontraron rutas.")
    }

    scanner.close() // Cerrar el archivo
}
