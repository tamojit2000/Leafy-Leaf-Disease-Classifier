package com.ysvg2tafy.leafy

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.common.TensorProcessor
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

var tflite: Interpreter? = null
private val tfliteModel: MappedByteBuffer? = null
private var inputImageBuffer: TensorImage? = null
private var imageSizeX = 0
private var imageSizeY = 0
private var outputProbabilityBuffer: TensorBuffer? = null
private var probabilityProcessor: TensorProcessor? = null
private val IMAGE_MEAN = 0f
private val IMAGE_STD = 1f
private val PROBABILITY_MEAN = 0f
private val PROBABILITY_STD = 255f

private var labels: List<String>? = null




private fun loadImage(bitmap: Bitmap): TensorImage? {
    // Loads bitmap into a TensorImage.
    inputImageBuffer!!.load(bitmap)

    // Creates processor for the TensorImage.
    var cropSize = Math.min(bitmap.width, bitmap.height)
    //println("LoadImage")
    //println(bitmap.width)
    //println(bitmap.height)
    //cropSize=28

    // TODO(b/143564309): Fuse ops inside ImageProcessor.
    val imageProcessor = ImageProcessor.Builder()
        //.add(ResizeWithCropOrPadOp(cropSize, cropSize))
        .add(ResizeOp(imageSizeX, imageSizeY, ResizeOp.ResizeMethod.BILINEAR))
        //.add(getPreprocessNormalizeOp())
        //.add(QuantizeOp(128.0f,1/128.0f))
        //.add(NormalizeOp(255f,1f))
        .add(NormalizeOp(0f,255f))
        .build()
    return imageProcessor.process(inputImageBuffer)
}

@Throws(IOException::class)
private fun loadmodelfile(activity: Activity,ModelName:String): MappedByteBuffer? {
    val fileDescriptor = activity.assets.openFd(ModelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel: FileChannel = inputStream.getChannel()
    val startoffset = fileDescriptor.startOffset
    val declaredLength = fileDescriptor.declaredLength
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, startoffset, declaredLength)
}

private fun getPreprocessNormalizeOp(): TensorOperator? {
    return NormalizeOp(IMAGE_MEAN, IMAGE_STD)
}

private fun getPostprocessNormalizeOp(): TensorOperator? {
    return NormalizeOp(PROBABILITY_MEAN, PROBABILITY_STD)
}


fun Classifier(context: Context,bitmap: Bitmap,LabelName:String,ModelName: String): Map<String, Float> {

    try {
        tflite = loadmodelfile(context as Activity,ModelName)?.let { Interpreter(it) }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }


    val imageTensorIndex = 0
    val imageShape: IntArray = tflite?.getInputTensor(imageTensorIndex)!!.shape() // {1, height, width, 3}


    imageSizeY = imageShape[1]
    imageSizeX = imageShape[2]

    //println("Initialisation")
    //println(imageSizeX)
    //println(imageSizeY)

    val imageDataType: DataType = tflite?.getInputTensor(imageTensorIndex)!!.dataType()

    //println("imdDT")
    //println(imageDataType)

    val probabilityTensorIndex = 0
    val probabilityShape: IntArray = tflite!!.getOutputTensor(probabilityTensorIndex).shape() // {1, NUM_CLASSES}

    val probabilityDataType: DataType = tflite!!.getOutputTensor(probabilityTensorIndex).dataType()

    inputImageBuffer = TensorImage(imageDataType)
    outputProbabilityBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType)
    probabilityProcessor = TensorProcessor.Builder()
                    //.add(getPostprocessNormalizeOp())
                    //.add(DequantizeOp(0f,1/255.0f))
                    //.add(NormalizeOp(0.5f,1f))
                    //.add(NormalizeOp(0f,1/255f))
                    .build()

    inputImageBuffer = bitmap.let { it1 -> loadImage(it1) }



    tflite!!.run(inputImageBuffer!!.buffer, outputProbabilityBuffer!!.getBuffer().rewind())

    try {
        labels = FileUtil.loadLabels(context, LabelName)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    val labeledProbability: Map<String, Float> = TensorLabel(labels!!, probabilityProcessor!!.process(outputProbabilityBuffer))
        .getMapWithFloatValue()
    /*val maxValueInMap: Float = Collections.max(labeledProbability.values)
    for ((key, value) in labeledProbability) {
        if (value == maxValueInMap) {
            classifytext.text=key
        }
    }*/
    //Log.d("Imgsize", imageSizeX.toString()+" "+ imageSizeY.toString())


    return labeledProbability


}