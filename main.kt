import java.lang.Exception

interface Successable<out Type> {
    fun ifSuccess(action: (Type) -> Unit)
    fun successOrNull(): Type?
    fun ifFail(action: () -> Unit)
    val isSuccess: Boolean
}
interface Failable<out Error> {
    val isFail: Boolean
    fun ifFail(action: (Error) -> Unit)
    fun ifSuccess(action: () -> Unit)
}

sealed class Result<out Type, out Error>: Successable<Type>, Failable<Error> {

    fun onSuccess(action: (Type) -> Unit): Result<Type, Error> {
        ifSuccess(action)
        return this
    }

    fun onFail(action: (Error) -> Unit): Result<Type, Error> {
        ifFail(action)
        return this
    }

    fun onSuccessUnit(action: () -> Unit): Result<Type, Error> {
        ifSuccess(action)
        return this
    }

    fun onFailUnit(action: () -> Unit): Result<Type, Error> {
        ifFail(action)
        return this
    }

    class Success<Type, Error>(val res: Type) : Result<Type, Error>() {
        override val isSuccess: Boolean = true
        override val isFail: Boolean = false
        override fun ifFail(action: () -> Unit) = Unit
        override fun ifSuccess(action: () -> Unit) = action()
        override fun ifSuccess(action: (Type) -> Unit) = action(res)
        override fun successOrNull(): Type? = res
        override fun ifFail(action: (Error) -> Unit) = Unit
    }

    class Failure<Type, Error>(val error: Error) : Result<Type, Error>() {
        override val isSuccess: Boolean = false
        override val isFail: Boolean = true
        override fun ifFail(action: () -> Unit) = action()
        override fun ifSuccess(action: () -> Unit) = Unit
        override fun ifSuccess(action: (Type) -> Unit) = Unit
        override fun successOrNull(): Type? = null
        override fun ifFail(action: (Error) -> Unit) = action(error)
    }

    companion object {
        fun <Type> successOnly(r: Type): Successable<Type> = Success<Type, Unit>(r)
        fun <Error> failOnly(r: Error): Failable<Error> = Failure<Unit, Error>(r)

        fun <Type, Error> success(r: Type): Result<Type, Error> = Success(r)
        fun <Type, Error> fail(e: Error): Result<Type, Error> = Failure(e)

        fun <Error> success(): Failable<Error> = Success(Unit)
        fun <Type> fail(): Successable<Type> = Failure(Unit)
    }
}


fun testSuccessOnly(check: Boolean): Successable<Int> {
    if (check) {
//        return Result.success<Int, Unit>(12)
        return Result.successOnly(12)
    } else {
        return Result.fail()
    }
}

fun testFailOnly(check: Boolean): Failable<Exception> {
    if (check) {
        return Result.success(12)
    } else {
        return Result.failOnly(java.lang.RuntimeException("asd"))
    }
}

fun testResult(check: Boolean): Result<Int, Exception> {
    if (check) {
        return Result.success(12)
    } else {
        return Result.fail(java.lang.RuntimeException("asd"))
    }
}

@Suppress("ThrowableNotThrown", "JoinDeclarationAndAssignment")
fun test() {

    val only = testSuccessOnly(false)
    only.ifFail {  }
    only.ifSuccess { it }
    only.successOrNull()
    

    val result = testResult(false)
    result
        .onSuccess { println(it) }
        .onFail { println(it.message) }
}

test()
