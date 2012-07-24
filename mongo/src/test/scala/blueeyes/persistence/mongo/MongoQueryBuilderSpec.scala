package blueeyes.persistence.mongo

import org.scalatest._
import MongoQueryBuilder._
import blueeyes.json.JPath
import blueeyes.json.JsonAST._

class MongoQueryBuilderSpec extends WordSpec with MustMatchers{
  private val jObject = JObject(JField("Foo", JString("bar")) :: Nil)

  "creates select query" in{
    select("foo", "bar").from("collection") must equal ( MongoSelectQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection") )
  }
  "creates selectAndUpdate query" in{
    selectAndUpdate("collection").set(jObject) must equal ( MongoSelectAndUpdateQuery("collection", jObject, None, None, MongoSelection(Set()), false, false) )
  }
  "creates selectAndUpsert query" in{
    selectAndUpsert("collection").set(jObject) must equal ( MongoSelectAndUpdateQuery("collection", jObject, None, None, MongoSelection(Set()), false, true) )
  }
  "creates selectAndRemove query" in{
    selectAndRemove.from("collection") must equal ( MongoSelectAndRemoveQuery("collection", None, None, MongoSelection(Set())) )
  }
  "creates group query" in{
    group(JObject(Nil), "dummy", "foo", "bar").from("collection") must equal ( MongoGroupQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection", JObject(Nil), "dummy") )
  }
  "creates mapReduce query" in{
    mapReduce("foo", "bar").from("collection") must equal ( MongoMapReduceQuery("foo", "bar",  "collection") )
  }
  "creates distinct query" in{
    distinct("foo").from("collection") must equal ( MongoDistinctQuery(JPath("foo"), "collection") )
  }
  "creates selectOne query" in{
    selectOne("foo", "bar").from("collection") must equal ( MongoSelectOneQuery(MongoSelection(Set(JPath("foo"), JPath("bar"))), "collection") )
  }
  "creates remove query" in{
    remove.from("collection") must equal ( MongoRemoveQuery("collection") )
  }
  "creates count query" in{
    count.from("collection") must equal ( MongoCountQuery("collection") )
  }
  "creates insert query" in{
    insert(jObject).into("collection") must equal ( MongoInsertQuery("collection", jObject :: Nil) )
  }
  "creates ensureIndex query" in{
    ensureIndex("index").on("address.city").in("collection") must equal ( MongoEnsureIndexQuery("collection", "index", List[Tuple2[JPath, IndexType]](Tuple2(JPath("address.city"), OrdinaryIndex)), false) )
  }
  "creates dropIndex query" in{
    dropIndex("index").in("collection") must equal ( MongoDropIndexQuery("collection", "index") )
  }
  "creates dropIndexes query" in{
    dropIndexes.in("collection") must equal ( MongoDropIndexesQuery("collection") )
  }
  "creates ensureUniqueIndex query" in{
    ensureUniqueIndex("index").on("address.city").in("collection") must equal ( MongoEnsureIndexQuery("collection", "index", List[Tuple2[JPath, IndexType]](Tuple2(JPath("address.city"), OrdinaryIndex)), true) )
  }
  "creates update query" in{
    update("collection").set(jObject) must equal ( MongoUpdateQuery("collection", jObject) )
  }
  "creates updateMany query" in{
    updateMany("collection").set(jObject) must equal ( MongoUpdateQuery("collection", jObject, None, false, true) )
  }
  "creates upsert query" in{
    upsert("collection").set(jObject) must equal ( MongoUpdateQuery("collection", jObject, None, true, false) )
  }
  "creates upsertMany query" in{
    upsertMany("collection").set(jObject) must equal ( MongoUpdateQuery("collection", jObject, None, true, true) )
  } 
}
