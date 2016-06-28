package controllers

import scala.concurrent.duration._
import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future, Await }

import javax.inject._
import it.innove.play.pdf.PdfGenerator
import play.api.data.format.Formats._ 


class ProductorController @Inject() (repo: ProductorRepository, repoModule: ModuleRepository, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  var modules = getModuleNamesMap()
  var interval = 30

  def getModuleNamesMap(): Map[String, String] = {
    Await.result(repoModule.getListNames().map{ case (res1) => 
      val cache = collection.mutable.Map[String, String]()
      res1.foreach{ case (key: Long, value: String) => 
        cache put (key.toString(), value)
      }
      println(cache)
      cache.toMap
    }, 3000.millis)
  }

  def getTotal(): Int = {
    Await.result(repo.getTotal().map{ case (res1) => 
      println(res1)
      res1
    }, 3000.millis)
  }

  def searchByAccount(account: String): Seq[Productor] = {
    Await.result(repo.getByAccount(account).map{ res => 
      res
    }, 1000.millis)
  }


  val newForm: Form[CreateProductorForm] = Form {
    mapping(
      "nombre" -> nonEmptyText,
      "carnet" -> number.verifying(min(0), max(9999999)),
      "telefono" -> number.verifying(min(0), max(9999999)),
      "direccion" -> nonEmptyText,
      "account" -> text,
      "module" -> longNumber
    )(CreateProductorForm.apply)(CreateProductorForm.unapply)
  }

  def index(start: Int) = Action {
    modules = getModuleNamesMap()
    var total = getTotal()
    var currentPage = 1
    Ok(views.html.productor_index(newForm, searchForm, modules, Seq[Productor](), total, currentPage, interval))
  }

  def search(search: String) = Action {
    val productors = searchByAccount(search)
    modules = getModuleNamesMap()
    var total = getTotal()
    var currentPage = 1
    Ok(views.html.productor_index(newForm, searchForm, modules, productors, total, currentPage, interval))
  }

  def searchProduct = Action.async { implicit request =>
    var total = getTotal()
    var currentPage = 1
    searchForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.productor_index(newForm, searchForm, modules, Seq[Productor](), total, currentPage, interval)))
      },
      res => {
          Future.successful(Redirect(routes.ProductorController.search(res.account)))
      }
    )
  }

  def index_pdf = Action {
    val generator = new PdfGenerator
    Ok(generator.toBytes(views.html.reporte_productores(), "http://localhost:9000/")).as("application/pdf")
  }

  def add = Action.async { implicit request =>
    var total = getTotal()
    var currentPage = 1
    newForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.productor_index(errorForm, searchForm, modules, Seq[Productor](), total, currentPage, interval)))
      },
      res => {
        repo.create (res.nombre, res.carnet, res.telefono, res.direccion,
                    res.account, res.module, modules(res.module.toString)).map { _ =>
          Redirect(routes.ProductorController.index(1))
        }
      }
    )
  }

  def getProductorsByModule(id: Long) = Action.async {
    repo.list(0, interval).map { res =>
      Ok(Json.toJson(res))
    }
  }

  def getProductores(page: Int) = Action.async {
    repo.list((page - 1) * interval, interval).map { res =>
      Ok(Json.toJson(res))
    }
  }

  def getProductoresReport = Action.async {
    repo.list(0, interval).map { res =>
      Ok(Json.toJson(res))
    }
  }

  // update required
  val updateForm: Form[UpdateProductorForm] = Form {
    mapping(
      "id" -> longNumber,
      "nombre" -> nonEmptyText,
      "carnet" -> number,
      "telefono" -> number,
      "direccion" -> nonEmptyText,
      "account" -> text,
      "module" -> longNumber,
      "totalDebt" -> of[Double],
      "numberPayment" -> number,
      "position" -> text
    )(UpdateProductorForm.apply)(UpdateProductorForm.unapply)
  }

  val searchForm: Form[SearchProductorForm] = Form {
    mapping(
      "account" -> text
    )(SearchProductorForm.apply)(SearchProductorForm.unapply)
  }

  // to copy
  def show(id: Long) = Action {
    Ok(views.html.productor_show())
  }

  // update required
  def getUpdate(id: Long) = Action.async {
    modules = getModuleNamesMap()
    repo.getById(id).map { res =>
      val anyData = Map(
        "id" -> id.toString().toString(),
        "nombre" -> res.toList(0).nombre,
        "carnet" -> res.toList(0).carnet.toString(),
        "telefono" -> res.toList(0).telefono.toString(),
        "direccion" -> res.toList(0).direccion,
        "account" -> res.toList(0).account.toString(),
        "module" -> res.toList(0).module.toString(),
        "totalDebt" -> res.toList(0).totalDebt.toString(),
        "numberPayment" -> res.toList(0).numberPayment.toString(),
        "position" -> res.toList(0).position.toString()
        )
      Ok(views.html.productor_update(updateForm.bind(anyData), modules))
    }
  }

  // delete required
  def delete(id: Long) = Action.async {
    var total = getTotal()
    var currentPage = 1
    repo.delete(id).map { res =>
      Ok(views.html.productor_index(newForm, searchForm, modules, Seq[Productor](), total, currentPage, interval))
    }
  }

  // to copy
  def getById(id: Long) = Action.async {
    repo.getById(id).map { res =>
      Ok(Json.toJson(res))
    }
  }

  // update required
  def updatePost = Action.async { implicit request =>
    updateForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.productor_update(errorForm, modules)))
      },
      res => {
        repo.update(
                      res.id, res.nombre, res.carnet, res.telefono,
                      res.direccion, res.account, res.module,
                      modules(res.module.toString()), "Asociacion Name",
                      res.totalDebt, res.numberPayment,
                      res.position
                    ).map { _ =>
          Redirect(routes.ProductorController.index(1))
        }
      }
    )
  }
}

case class CreateProductorForm(
                                nombre: String, carnet: Int, telefono: Int,
                                direccion: String, account: String, module: Long
                              )

case class UpdateProductorForm(
                                id: Long, nombre: String, carnet: Int, telefono: Int,
                                direccion: String, account: String, module: Long,
                                totalDebt: Double, numberPayment: Int, position: String
                              )

case class SearchProductorForm (account: String)
