class C1

class C2 {
    val p1 = 0

    init { }

    constructor() {}

    fun f1() {}

    companion object
}

class C3 {
    companion object

    fun f1() {}

    constructor() {}

    val p1 = 0

    init {}
}

// expect
// 18:5:Method should be before companion object
// 20:5:Secondary constructor should be before companion object
// 22:5:Property or initializer should be before companion object
// 24:5:Property or initializer should be before companion object
