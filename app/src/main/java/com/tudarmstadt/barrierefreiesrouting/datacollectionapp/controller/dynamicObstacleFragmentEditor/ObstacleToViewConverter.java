package com.tudarmstadt.barrierefreiesrouting.datacollectionapp.controller.dynamicObstacleFragmentEditor;

import android.content.Context;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import bp.common.model.AttributeTypes;
import bp.common.model.annotations.EditableAttribute;
import bp.common.model.obstacles.Obstacle;

/**
 * Converts the Obstacle to a ObstacleViewModel.
 */
public class ObstacleToViewConverter {

    /**
     * Used to access the corresponding converter for the given class.
     */
    private static Map<Class, Converter> converterForClass = new HashMap<>();

    static {
        converterForClass.put(Long.TYPE, new Converter() {
            @Override
            public ObstacleAttribute<?> convert(Object value, String name) {
                ObstacleAttribute<Long> oa = new ObstacleAttribute<Long>(Long.TYPE, name);
                oa.value = (Long) value;
                return oa;
            }
        });
        converterForClass.put(Integer.TYPE, new Converter() {
            @Override
            public ObstacleAttribute<?> convert(Object value, String name) {
                ObstacleAttribute<Integer> oa = new ObstacleAttribute<Integer>(Integer.TYPE, name);
                oa.value = (Integer) value;
                return oa;
            }
        });
        converterForClass.put(Double.TYPE, new Converter() {
            @Override
            public ObstacleAttribute<?> convert(Object value, String name) {
                ObstacleAttribute<Double> oa = new ObstacleAttribute<Double>(Double.TYPE, name);
                oa.value = (Double) value;
                return oa;

            }
        });
        converterForClass.put(String.class, new Converter() {
            @Override
            public ObstacleAttribute<?> convert(Object value, String name) {
                ObstacleAttribute<String> oa = new ObstacleAttribute<String>(String.class, name);
                oa.value = (String) value;
                return oa;
            }
        });
    }

    /**
     * Converts all Attributes with the "EditableAttribute" Annotation from the given Obstacle
     * instance, to a Mapping between AttributeName (stored in the EditableAttribute Class as value)
     * and the ObstacleAttribute.
     *
     * @param obstacle the given obstacle to convert
     * @return a mapping of attribute names to ObstacleAttribute instances
     */
    public static ObstacleViewModel convertObstacleToViewModel(Obstacle obstacle, Context ctx) {

        ObstacleViewModel result = new ObstacleViewModel();

        HashMap<String, ObstacleAttribute<?>> map = new HashMap<String, ObstacleAttribute<?>>();
        Class<?> current = obstacle.getClass();
        // Set the specific class of the obstacle (e.g. Stairs)
        result.internalObstacle = obstacle;
        while (current.getSuperclass() != null) {
            Field[] fieldsOfObstacle = current.getDeclaredFields();

            for (int i = 0; i < fieldsOfObstacle.length; i++) {
                Field f = fieldsOfObstacle[i];
                // Unsupported Attribute Object Types will be skipped.
                // Implement a converter e.g. for boolean
                if (converterForClass.get(f.getType()) != null)
                    try {
                        f.setAccessible(true);
                        if (f.getAnnotation(EditableAttribute.class) != null) {
                            String attributeName = f.getAnnotation(EditableAttribute.class).name();
                            ObstacleAttribute<?> attribute = converterForClass.get(f.getType()).convert(f.get(obstacle), attributeName);
                            String validOptions = f.getAnnotation(EditableAttribute.class).validoptions();

                            if(f.getAnnotation(EditableAttribute.class).type().equals(AttributeTypes.DROPDOWN) && !validOptions.isEmpty()){
                                attribute.validOptions = validOptions;
                                attribute.isDrowpdown = true;
                            }
                            map.put(attributeName, attribute);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
            current = current.getSuperclass();
        }
        // save the obstacleAttributes map to the view model
        result.attributesMap = map;

        return result;
    }

    public static Obstacle convertAttributeMapToObstacle(ObstacleViewModel viewModel) {
        Obstacle result = viewModel.internalObstacle;


        Class<?> current = result.getClass();
        while (current.getSuperclass() != null) {
            Field[] fieldsOfObstacle = current.getDeclaredFields();
            for (int i = 0; i < fieldsOfObstacle.length; i++) {
                Field f = fieldsOfObstacle[i];
                f.setAccessible(true);
                if (f.getAnnotation(EditableAttribute.class) != null) {
                    String attributeName = f.getAnnotation(EditableAttribute.class).name();

                    ObstacleAttribute<?> attribute = viewModel.attributesMap.get(attributeName);
                    if (attribute != null) {
                        try {
                            f.set(result, attribute.value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }


                }

            }
            current = current.getSuperclass();


        }
        return result;

    }


    /**
     * value is the Field, name
     */
    interface Converter {
        ObstacleAttribute<?> convert(Object value, String name);
    }

}
